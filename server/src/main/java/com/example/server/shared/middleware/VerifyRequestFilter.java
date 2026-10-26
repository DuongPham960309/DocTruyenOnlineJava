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

public class VerifyRequestFilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        long serverTime = System.currentTimeMillis();
        String xKey = request.getHeader("x-key");
        if (xKey == null || xKey.isEmpty()) {
            xKey = request.getParameter("x-key");
        }

        if (xKey == null || xKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        try {
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
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_OK);
        }
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
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}