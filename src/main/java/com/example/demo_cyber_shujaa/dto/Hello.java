package com.example.demo_cyber_shujaa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hello {

    @NotEmpty(message = "Message cannot be empty")
    @JsonProperty("message")
    private String message;

    @JsonProperty("names")
    @NotEmpty(message = "list of names cannot be empty")
    private List<String> names;

    @JsonProperty("phoneNumber")
    @NotNull(message = "Phone number cannot be null")
    @Pattern(regexp = "^254\\d{9}$", message = "Phone number must start with 254 and be 12 digits long")
    private Long phoneNumber;

    @JsonProperty("callbackUrl")
    @NotNull(message = "Callback URL cannot be null")
    @URL(message = "Callback URL must be a valid URL")
    private String callbackUrl;
}
