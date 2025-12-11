package com.example.mobitel.Dao;

import com.example.mobitel.Dto.ProjectDto;

import java.util.List;

public interface ProjectDao {
    ProjectDto findProject(String project_name, String status);

    int AddProject(ProjectDto request);

    ProjectDto searchProject(String project_name, String status);

    List<ProjectDto> viewProjects();

    int  countPendingProjects();
}
