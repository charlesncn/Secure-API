package com.example.demo_cyber_shujaa.exceptions;

import lombok.Getter;

@Getter
public enum ErrorMsg {
    INVALID_REQUEST("Invalid request", 3000),
    TOKEN_EXPIRED("Token expired", 2000),
    INVALID_TOKEN("Invalid toke", 20001),
    ILLEGAL_ARGUMENT("Illegal arguments passed", 2003), USER_NOT_FOUND("User not found", 3002);
    private final String msg;
    private final int statusCode;


    ErrorMsg(String msg, int statusCode){
        this.msg = msg;
        this.statusCode = statusCode;
    }
}
