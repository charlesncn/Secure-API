package com.example.demo_cyber_shujaa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"statusCode", "message", "accessToken"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIResponse {

    private int statusCode;
    private String message;
    private String accessToken;
    private String refreshToken;


    public static APIResponse successResponse(String accessToken, String refreshToken) {
        APIResponse response = new APIResponse();
        response.setStatusCode(200);
        response.setMessage("Success");
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        return response;
    }

    public static APIResponse errorResponse(int statusCode, String message) {
        APIResponse response = new APIResponse();
        response.setStatusCode(statusCode);
        response.setMessage(message);
        return response;
    }

}
