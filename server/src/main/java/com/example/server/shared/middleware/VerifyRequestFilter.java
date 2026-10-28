package com.example.server.shared.middleware;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.security.NoSuchAlgorithmException;

public class VerifyRequestFilter implements Filter {
    public void doFilter(
        ServletRequest req, ServletResponse res, FilterChain chain
    ) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;

        long serverTime = System.currentTimeMillis();
        String xKey = getValidXKey((HttpServletRequest) req);

        if (xKey == null) {
            response.setStatus(HttpServletResponse.SC_OK);

            return;
        }

        Object[] parsed = getClientSignatureAndTimeMS(xKey);
        String clientSignature = (String) parsed[0];
        long clientTime = (Long) parsed[1];

        long timeDifference = Math.abs(serverTime - clientTime);
        String serverSignature = sha256(clientTime + "|ConstantString");

        if (timeDifference < 10000 && serverSignature.equalsIgnoreCase(clientSignature)) {
            chain.doFilter(req, res);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    private String getValidXKey(HttpServletRequest request) {
        final int X_KEY_LENGTH = 80;
        String xKey = request.getHeader("x-key");

        if (xKey == null) {
            xKey = request.getParameter("x-key");
        }

        if (xKey == null || xKey.length() != X_KEY_LENGTH) {
            return null;
        }

        for (int i = 0; i < xKey.length(); i++) {
            char c = xKey.charAt(i);
            boolean ok = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');

            if (!ok) {
                return null;
            }
        }

        return xKey;
    }

    private Object[] getClientSignatureAndTimeMS(String key) {
        int baseHexadecimal = 16;
        int timeOffsetPosition = Integer.parseInt(String.valueOf(key.charAt(0)), baseHexadecimal);
        int timeStart = 1 + timeOffsetPosition;
        int timeLength = 16;

        String clientSignature = key.substring(0, timeStart) + key.substring(timeStart + timeLength);

        String timeObfuscateStr = key.substring(timeStart, timeStart + timeLength);
        BigInteger timeObfuscate = new BigInteger(timeObfuscateStr, 16);

        BigInteger mask64 = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);
        BigInteger inverted = timeObfuscate.xor(mask64);

        int secretHexPosition = Integer.parseInt(String.valueOf(clientSignature.charAt(7)), baseHexadecimal);
        String secretHexStr = clientSignature.substring(secretHexPosition, secretHexPosition + timeLength);
        BigInteger secretHex = new BigInteger(secretHexStr, 16);

        long timeMS = inverted.xor(secretHex).longValue();

        return new Object[]{clientSignature, timeMS};
    }

    private String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            String hex = HexFormat.of().formatHex(hash);

            return hex;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 không khả dụng", ex);
        }
    }
}