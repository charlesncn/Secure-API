package com.example.demo_cyber_shujaa.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class CustomHttpMsgConverter extends AbstractHttpMessageConverter<Object> {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final Logger logger = Logger.getLogger(CustomHttpMsgConverter.class.getName());
        @Resource
    private final ObjectMapper objectMapper;
    private final EncryptDecrypt encryptDecrypt;
    @Value("${aes.encryption.at-transit.key}")
    private String secretKey;
    @Value("${aes.encryption.at-transit.enabled}")
    private boolean encryptionEnabled;
    @Setter
    @Resource
    private HttpServletRequest request;

    public CustomHttpMsgConverter(ObjectMapper objectMapper, EncryptDecrypt encryptDecrypt) {
        super(MediaType.APPLICATION_JSON,
                new MediaType("application", "*+json", DEFAULT_CHARSET));
        this.objectMapper = objectMapper;
        this.encryptDecrypt = encryptDecrypt;
    }

    @Override
    protected boolean supports(@NonNull Class<?> clazz) {
        return true;
    }

    @Override
    @NonNull
    protected Object readInternal(@NonNull Class<?> clazz, @NonNull HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        String requestPath = request.getRequestURI();
        // Default behavior: Do not decrypt
        if (isSwaggerEndpoint(requestPath))
            return objectMapper.readValue(inputMessage.getBody(), clazz);
        // Decrypt the request if encryption is enabled
        if (encryptionEnabled)
            return objectMapper.readValue(decrypt(inputMessage.getBody()), clazz);
        // Do not decrypt if encryption is disabled
        return objectMapper.readValue(inputMessage.getBody(), clazz);
    }

    @Override
    protected void writeInternal(@NonNull Object o, @NonNull HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        String requestPath = request.getRequestURI();
        if (isSwaggerEndpoint(requestPath)) {
            // Do not Encrypt the response
            outputMessage.getBody().write(objectMapper.writeValueAsBytes(o));
            return;
        }
        if (encryptionEnabled) {
            logger.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o));
            // Encrypt the response if encryption is enabled
            outputMessage.getBody().write(encrypt(objectMapper.writeValueAsBytes(o)));
            return;
        }
        // Do not encrypt if encryption is disabled
        outputMessage.getBody().write(objectMapper.writeValueAsBytes(o));
    }

    /**
     * requests params of any API
     *
     * @param inputStream inputStream
     * @return inputStream
     */
    protected InputStream decrypt(InputStream inputStream) {
        String requestParamString = readInputStream(inputStream);
        String origText = getOrigText(requestParamString);
        Object decryptRequestString = getDecryptedRequest(origText);
        return getInputStreamFromDecryptedRequest(decryptRequestString);
    }

    protected String readInputStream(InputStream inputStream) {
        StringBuilder requestParamString = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            int c;
            while ((c = reader.read()) != -1) {
                requestParamString.append((char) c);
            }
        } catch (IOException e) {
            logger.warning(e.toString());
        }
        return requestParamString.toString();
    }

    protected String getOrigText(String requestParamString) {
        String origText;
        try {
            JSONObject requestJsonObject = new JSONObject(requestParamString.replace("\n", ""));
            origText = requestJsonObject.getString("data");
        } catch (Exception ex) {
            logger.warning("Not Encrypted Request!");
            logger.warning(Arrays.stream(ex.getStackTrace()).toString());
            origText = null;
        }
        return origText;
    }

    protected Object getDecryptedRequest(String origText) {
        Object decryptRequestString = null;
        if (origText != null) {
            decryptRequestString = encryptDecrypt.decrypt(origText, secretKey); // Calls Decrypt
            try {
                logger.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(decryptRequestString));
            } catch (JsonProcessingException e) {
                logger.warning(e.toString());
            }
        }
        return decryptRequestString;
    }

    protected InputStream getInputStreamFromDecryptedRequest(Object decryptRequestString) {
        if (decryptRequestString != null) {
            return new ByteArrayInputStream(decryptRequestString.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            return null;
        }
    }

    /**
     * response of API
     *
     * @param bytesToEncrypt byte array of response
     * @return byte array of response
     */
    protected byte[] encrypt(byte[] bytesToEncrypt) {
        // do your encryption here
        String apiJsonResponse = new String(bytesToEncrypt);

        String encryptedString = encryptDecrypt.encrypt(apiJsonResponse, secretKey);
        if (encryptedString != null) {
            Map<String, String> hashMap = new HashMap<>();
            hashMap.put("data", encryptedString);
            JSONObject jsonObj = new JSONObject(hashMap);
            return jsonObj.toString().getBytes();
        }
        return bytesToEncrypt;
    }

    protected boolean isSwaggerEndpoint(String requestPath) {
        return Arrays.asList("swagger-ui", "v2/api-docs", "v3/api-docs", "/swagger-resources",
                "/configuration/ui", "/configuration/security", "/webjars").contains(requestPath);
    }
}