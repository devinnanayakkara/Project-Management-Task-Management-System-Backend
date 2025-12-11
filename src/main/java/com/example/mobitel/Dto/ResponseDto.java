package com.example.mobitel.Dto;

import lombok.Data;

@Data
public class ResponseDto {
    private String Status;
    private String ReturnCode;
    private String Description;
    private Object content;
}
