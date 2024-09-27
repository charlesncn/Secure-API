package com.example.demo_cyber_shujaa.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.util.SerializationUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Base64;
import java.util.logging.Logger;

@Configuration
@Converter
public class AesEncryptor implements AttributeConverter<Object, String> {

    private static final String ENCRYPTION_CIPHER = "AES";
    private final Logger logger = Logger.getLogger(AesEncryptor.class.getName());
    @Value("${aes.encryption.at-rest.key}")
    private String encryptionKey;
    private Key key;
    private Cipher cipher;


    protected Key getKey() {
        if (key == null)
            key = new SecretKeySpec(encryptionKey.getBytes(), ENCRYPTION_CIPHER);
        return key;
    }

    protected Cipher getCipher() throws GeneralSecurityException {
        if (cipher == null)
            cipher = Cipher.getInstance(ENCRYPTION_CIPHER);
        return cipher;
    }

    protected void initCipher(int encryptMode) throws GeneralSecurityException {
        getCipher().init(encryptMode, getKey());
    }

    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(Object attribute) {
        if (attribute == null)
            return null;
        initCipher(Cipher.ENCRYPT_MODE);
        byte[] bytes = serialize(attribute);
        return Base64.getEncoder().encodeToString(getCipher().doFinal(bytes));
    }

    protected byte[] serialize(Object attribute) {
        return SerializationUtils.serialize(attribute);
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        try {
            initCipher(Cipher.DECRYPT_MODE);
        } catch (GeneralSecurityException e) {
            logger.warning(e.toString());
            return null;
        }
        byte[] bytes;
        try {
            bytes = getCipher().doFinal(Base64.getDecoder().decode(dbData));
        } catch (GeneralSecurityException e) {
            logger.warning(e.toString());
            return null;
        }
        return deserialize(bytes);
    }

    public Object deserialize(@Nullable byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            throw new IllegalArgumentException("Could not deserialize object", ex);
        }
    }
}