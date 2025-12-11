package com.example.mobitel.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;


import lombok.Data;



import java.time.LocalDateTime;

@Data

// User Data Transfer Object to transfer user data between layers
public class UserDto {
    private String username;
    private String id;

    private String password;
    private String addedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime addedDate;

    private String requestedUserId;
    private LocalDateTime terminatedOn;
    private String terminatedBy;
    private String user_id;
    private String user_role;
}
