package com.heejong.hr.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {

    @Value("${encryption.secret:your-256-bit-secret-key-for-encryption-change-this-in-production}")
    private String secretKey;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_LENGTH = 32; // 256 bits = 32 bytes
    private static final int IV_LENGTH = 16; // AES 블록 크기

    /**
     * 키를 SHA-256 해시로 변환하여 항상 32바이트로 만듦
     */
    private byte[] getKeyBytes() {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = secretKey.getBytes(StandardCharsets.UTF_8);
            byte[] hashedKey = sha.digest(key);
            // 32바이트로 제한 (AES-256)
            return Arrays.copyOf(hashedKey, KEY_LENGTH);
        } catch (Exception e) {
            throw new RuntimeException("키 생성 실패", e);
        }
    }

    /**
     * IV를 키에서 생성 (일관성 유지)
     */
    private byte[] getIV() {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] key = secretKey.getBytes(StandardCharsets.UTF_8);
            byte[] iv = md5.digest(key);
            return Arrays.copyOf(iv, IV_LENGTH);
        } catch (Exception e) {
            throw new RuntimeException("IV 생성 실패", e);
        }
    }

    /**
     * 연봉 정보 암호화
     */
    public String encrypt(String data) {
        try {
            if (data == null || data.isEmpty()) {
                throw new IllegalArgumentException("암호화할 데이터가 없습니다.");
            }

            byte[] keyBytes = getKeyBytes();
            byte[] iv = getIV();

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // IV와 암호화된 데이터를 함께 저장
            byte[] encryptedWithIV = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, encryptedWithIV, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, encryptedWithIV, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(encryptedWithIV);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("암호화 패딩 오류: " + e.getMessage() + " - 알고리즘: " + ALGORITHM, e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("암호화 알고리즘 오류: " + e.getMessage() + " - 알고리즘: " + ALGORITHM, e);
        } catch (java.security.InvalidKeyException e) {
            throw new RuntimeException("암호화 키 오류: " + e.getMessage() + " - 키 길이: " + getKeyBytes().length, e);
        } catch (java.security.InvalidAlgorithmParameterException e) {
            throw new RuntimeException("암호화 파라미터 오류: " + e.getMessage(), e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException("암호화 블록 크기 오류: " + e.getMessage(), e);
        } catch (BadPaddingException e) {
            throw new RuntimeException("암호화 패딩 오류: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("암호화 실패: " + e.getClass().getName() + " - " + e.getMessage(), e);
        }
    }

    /**
     * 연봉 정보 복호화
     */
    public String decrypt(String encryptedData) {
        try {
            if (encryptedData == null || encryptedData.isEmpty()) {
                throw new IllegalArgumentException("복호화할 데이터가 없습니다.");
            }
            byte[] encryptedWithIV = Base64.getDecoder().decode(encryptedData);

            // IV와 암호화된 데이터 분리
            byte[] iv = Arrays.copyOfRange(encryptedWithIV, 0, IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(encryptedWithIV, IV_LENGTH, encryptedWithIV.length);

            byte[] keyBytes = getKeyBytes();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("복호화 패딩 오류: " + e.getMessage() + " - 알고리즘: " + ALGORITHM, e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("복호화 알고리즘 오류: " + e.getMessage() + " - 알고리즘: " + ALGORITHM, e);
        } catch (java.security.InvalidKeyException e) {
            throw new RuntimeException("복호화 키 오류: " + e.getMessage(), e);
        } catch (java.security.InvalidAlgorithmParameterException e) {
            throw new RuntimeException("복호화 파라미터 오류: " + e.getMessage(), e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException("복호화 블록 크기 오류: " + e.getMessage(), e);
        } catch (BadPaddingException e) {
            throw new RuntimeException("복호화 패딩 오류: " + e.getMessage() + " - 데이터가 손상되었거나 키가 잘못되었을 수 있습니다.", e);
        } catch (Exception e) {
            throw new RuntimeException("복호화 실패: " + e.getClass().getName() + " - " + e.getMessage(), e);
        }
    }
}
