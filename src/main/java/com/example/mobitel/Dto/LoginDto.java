package com.example.mobitel.Dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LoginDto {
    private String requestUserId;

    @JsonIgnore
    private String requestUserPassword;

    private String userType;
    private String roleCode;
    private String status;
    private String id;

    @JsonIgnore
    private String user_role;
    private String added_by;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate added_on;

    private String terminated_by;
    private LocalDateTime terminated_on;

    @JsonIgnore
    private String password;

    private String username;

    private String email;


    private String systemUserId;
    private String user_id;

}
