package com.example.demo_cyber_shujaa.util;

import com.example.demo_cyber_shujaa.dto.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Base64;
import java.util.logging.Logger;

@Component
public class EncryptDecrypt {
    private static final String TRANSFORM = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM = "AES";

    private static final Logger logger = Logger.getLogger(EncryptDecrypt.class.getName());

    public String encryptText(Object origText, String publicKey) {
        try {
            byte[] plainTextData = origText.toString().getBytes();
            byte[] secretKey = publicKey.getBytes();
            String iv = new String(secretKey).substring(0, 16);

            Cipher cipher = Cipher.getInstance(TRANSFORM);

            int plaintextLength = plainTextData.length;
            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(plainTextData, 0, plaintext, 0, plainTextData.length);

            return getKeySpecString(secretKey, iv, cipher, plaintext);

        } catch (Exception e) {
            logger.warning(e.toString());
            return null;
        }
    }

    @NotNull
    private String getKeySpecString(byte[] secretKey, String iv, Cipher cipher, byte[] plaintext) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec keySpec = new SecretKeySpec(secretKey, ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(plaintext);

        return new String(Base64.getEncoder().encode(encrypted));
    }

    public Object decrypt(String cipherText, String publicKey) {
        if (cipherText == null)
            throw new IllegalArgumentException("Cipher text cannot be null");
        if (publicKey == null)
            throw new IllegalArgumentException("Public key cannot be null");
        try {
            byte[] cipherTextData = Base64.getDecoder().decode(cipherText);
            byte[] secretKey = publicKey.getBytes();
            String iv = new String(secretKey).substring(0, 16);

            Cipher cipher = Cipher.getInstance(TRANSFORM);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] originalBytes = cipher.doFinal(cipherTextData);
            String original = new String(originalBytes);
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readTree(original);
        } catch (Exception e) {
            logger.warning(e.toString());

            return Response.ofFailure(1200, "Failed to decrypt");
        }
    }

    public String encrypt(Object origText, String publicKey) {
        try {
            byte[] plainTextData;

            if (origText instanceof String txt) {
                plainTextData = txt.getBytes();
            } else if (origText instanceof Serializable) {
                plainTextData = serializeObject(origText);
            } else {
                throw new IllegalArgumentException("Unsupported data type for encryption");
            }

            byte[] secretKey = publicKey.getBytes();
            String iv = new String(secretKey).substring(0, 16);

            Cipher cipher = Cipher.getInstance(TRANSFORM);
            return getKeySpecString(secretKey, iv, cipher, plainTextData);

        } catch (Exception e) {
            logger.warning(e.toString());
            return null;
        }
    }

    protected byte[] serializeObject(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            return bos.toByteArray();
        }
    }
}