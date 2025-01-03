package com.example.userservice.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class KakaoTokenDto {

    private String access_token;
    private String token_type;
    private String refresh_token;
    private String expires_in;
    private String scope;
    private String refresh_token_expires_in;
}
