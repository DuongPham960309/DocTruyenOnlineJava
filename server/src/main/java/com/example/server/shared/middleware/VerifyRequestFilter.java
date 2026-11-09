package com.example.server.shared.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class VerifyRequestFilter extends OncePerRequestFilter {
    private static final long MAX_TIME_DIFFERENCE_MS = 10_000;
    private static final Pattern X_KEY_PATTERN = Pattern.compile("[0-9a-fA-F]{80}");
    private static final int HEX = 16;
    private static final int TIME_LENGTH = 16;
    private static final BigInteger MASK_64 = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);

    private record ClientKey(String signature, long timeMs) {}

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain chain
    ) throws ServletException, IOException {
        long serverTime = System.currentTimeMillis();
        String xKey = getValidXKey(request);

        if (xKey == null) {
            response.setStatus(HttpServletResponse.SC_OK);

            return;
        }

        ClientKey client = getClientSignatureAndTimeMS(xKey);
        boolean timeOk = Math.abs(serverTime - client.timeMs) < MAX_TIME_DIFFERENCE_MS;
        String serverSignature = sha256(client.timeMs + "|ConstantString");
        boolean signatureOk = MessageDigest.isEqual(
            serverSignature.getBytes(StandardCharsets.UTF_8),
            client.signature.getBytes(StandardCharsets.UTF_8)
        );
        // Lưu ý: Phương thức equals của class String dễ bị tấn công thời gian (timing attack).
        // Phương thức này dừng ngay khi gặp ký tự đầu tiên khác nhau, nên kẻ tấn công có thể
        // đo thời gian phản hồi để đoán dần từng ký tự.

        if (timeOk && signatureOk) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    private String getValidXKey(HttpServletRequest request) {
        String xKey = request.getHeader("x-key");

        if (xKey == null) {
            xKey = request.getParameter("x-key");
        }

        return (xKey != null && X_KEY_PATTERN.matcher(xKey).matches()) ? xKey : null;
    }

    private ClientKey getClientSignatureAndTimeMS(String key) {
        int timeStart = 1 + Character.digit(key.charAt(0), HEX);
        String signature = key.substring(0, timeStart) + key.substring(timeStart + TIME_LENGTH);

        String timeObfuscateStr = key.substring(timeStart, timeStart + TIME_LENGTH);
        BigInteger timeObfuscate = new BigInteger(timeObfuscateStr, HEX);
        BigInteger inverted = timeObfuscate.xor(MASK_64);

        int secretPosition = Character.digit(signature.charAt(7), HEX);
        String secretHexStr = signature.substring(secretPosition, secretPosition + TIME_LENGTH);
        BigInteger secretHex = new BigInteger(secretHexStr, HEX);

        long timeMS = inverted.xor(secretHex).longValue();

        return new ClientKey(signature, timeMS);
    }

    private String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 không khả dụng", ex);
        }
    }
}