package com.example.mobitel.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectDto {

    private Integer project_id;
    private String project_name;
    private String project_description;

    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate start_date;

    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate expected_end_date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate actual_end_date;


    private String added_by;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate added_on;

    private String status;
    private String terminatedBy;
    private LocalDateTime terminatedOn;

    private String status_name;
}
