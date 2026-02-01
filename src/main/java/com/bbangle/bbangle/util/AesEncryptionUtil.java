package com.bbangle.bbangle.util;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AesEncryptionUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String AES = "AES";

    private final SecretKeySpec secretKeySpec;
    private final IvParameterSpec ivParameterSpec;

    /**
     * Create an AesEncryptionUtil configured with the provided AES secret key.
     *
     * @param secretKey the AES secret key string; when encoded as UTF-8 it must be 16, 24, or 32 bytes long
     * @throws IllegalArgumentException if the UTF-8 encoding of {@code secretKey} does not produce 16, 24, or 32 bytes
     */
    public AesEncryptionUtil(@Value("${encryption.aes.secret-key}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException("AES key must be 16, 24, or 32 bytes");
        }
        this.secretKeySpec = new SecretKeySpec(keyBytes, AES);
        this.ivParameterSpec = new IvParameterSpec(keyBytes, 0, 16);
    }

    /**
     * Encrypts the given plaintext using AES/CBC/PKCS5Padding and produces Base64-encoded ciphertext.
     *
     * @param plainText the UTF-8 string to encrypt; if null or empty, the same value is returned unchanged
     * @return the Base64-encoded ciphertext corresponding to the input plaintext
     * @throws BbangleException if encryption fails
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new BbangleException(BbangleErrorCode.ENCRYPTION_FAILED);
        }
    }

    /**
     * Decrypts a Base64-encoded AES/CBC/PKCS5Padding ciphertext and returns the resulting UTF-8 plaintext.
     *
     * @param encryptedText the Base64-encoded ciphertext to decrypt; if {@code null} or empty, the same value is returned
     * @return the decrypted plaintext as a UTF-8 string
     * @throws BbangleException if decryption fails
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BbangleException(BbangleErrorCode.DECRYPTION_FAILED);
        }
    }
}