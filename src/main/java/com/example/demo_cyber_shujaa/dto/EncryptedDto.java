package com.example.demo_cyber_shujaa.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EncryptedDto {
    private String data;

    public static EncryptedDto data(String encryptedData) {
        EncryptedDto response = new EncryptedDto();
        response.setData(encryptedData);
        return response;
    }
}
