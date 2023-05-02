package com.shinkai.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserResponse {
    private String id;
    private String name;
    private String avatar;
    private String job;
    private String email;
    private String createdAt;
    private String userName;
}
