package com.example.mobitel.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class TaskDto {
    private String task_id;
    private String task_name;
    private String task_description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate start_date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate expected_end_date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate actual_end_date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate added_on;


    private String added_by;

    private String project_name;
    private String terminated_by;
    private String terminated_on;
    private Integer project_id;
    private String user_id;
    private String status;

    private String assign_to_username;

    private String completed_by;

    private LocalDateTime completed_on;

    private String assign_to;

    private String comment;

    private String old_user;
    private String new_user;
    private String swapped_by;
    private String current_user;
    private String status_id;
    private String status_name;

}
