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
@JsonPropertyOrder({
        "statusCode",
        "message"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private int statusCode;
    private String message;

    public static Response ofSuccess() {
        return new Response(0, "success");
    }

    public static Response ofSuccess(int code, String message) {
        return new Response(code, message);
    }

    public static Response ofFailure(int code, String msg) {
        return new Response(code, msg);
    }
}
