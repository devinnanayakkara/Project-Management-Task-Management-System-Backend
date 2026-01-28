package com.example.mobitel.Dto;

import lombok.Data;

@Data
public class DashboardCountDto {

    // Manager dashboard
    private int totalUsers;
    private int assignedTasks;
    private int activeTasks;
    private int pendingProjects;
    private int completedTasks;

    // Employee dashboard
    private int employeeTotalTasks;
    private int employeeAssignedTasks;
    private int UnassignedTasks;

    // Intern dashboard
    private int internTotalTasks;
    private int internActiveTasks;


}

