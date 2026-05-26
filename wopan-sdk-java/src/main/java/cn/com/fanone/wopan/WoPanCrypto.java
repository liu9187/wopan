package cn.com.fanone.wopan;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

final class WoPanCrypto {

    private static final byte[] IV = "wNSOYIB1k1DjY5lA".getBytes(StandardCharsets.UTF_8);

    private final ObjectMapper objectMapper;

    WoPanCrypto(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    String encryptParam(String channel, Map<String, Object> payload, String accessToken) {
        if (payload == null) {
            return null;
        }
        byte[] json = toJsonBytes(payload);
        byte[] key = determineKey(channel, accessToken);
        byte[] encrypted = aes(json, key, Cipher.ENCRYPT_MODE);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    String decryptPayload(String channel, String data, String accessToken) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        byte[] key = determineKey(channel, accessToken);
        byte[] decoded = Base64.getDecoder().decode(data);
        byte[] decrypted = aes(decoded, key, Cipher.DECRYPT_MODE);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private byte[] determineKey(String channel, String accessToken) {
        if (WoPanConstants.CHANNEL_API_USER.equals(channel)) {
            return WoPanConstants.DEFAULT_CLIENT_SECRET.getBytes(StandardCharsets.UTF_8);
        }
        String token = Objects.requireNonNull(accessToken, "Access token required before calling this API");
        if (token.length() < 16) {
            throw new WoPanException("Access token shorter than 16 characters");
        }
        return token.substring(0, 16).getBytes(StandardCharsets.UTF_8);
    }

    private byte[] aes(byte[] body, byte[] key, int mode) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(mode, keySpec, new IvParameterSpec(IV));
            return cipher.doFinal(body);
        } catch (Exception ex) {
            throw new WoPanException("AES operation failed", ex);
        }
    }

    private byte[] toJsonBytes(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsBytes(payload);
        } catch (Exception ex) {
            throw new WoPanException("Unable to serialize payload", ex);
        }
    }
}
