// FILE 26: EncryptionUtil.java
package com.eduplatform.security.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "MySecretKey123456"; // Change in production

    /**
     * ENCRYPT TEXT
     */
    public static String encrypt(String plainText) {
        try {
            SecretKeySpec key = new SecretKeySpec(
                    SECRET_KEY.getBytes(), 0, SECRET_KEY.getBytes().length, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * DECRYPT TEXT
     */
    public static String decrypt(String encryptedText) {
        try {
            SecretKeySpec key = new SecretKeySpec(
                    SECRET_KEY.getBytes(), 0, SECRET_KEY.getBytes().length, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}