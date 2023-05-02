package com.shinkai.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserBody {
    private String name;
    private String avatar;
    private String job;
    private String email;
    private String userName;
}
