package com.example.demo_cyber_shujaa.exceptions;

public class CustomException extends Exception {

    private final ErrorMsg errorMsg;

    public int errorCode() {
        return errorMsg.getStatusCode();
    }

    public CustomException(ErrorMsg errorMsg) {
        super(errorMsg.getMsg());
        this.errorMsg = errorMsg;
    }
}
