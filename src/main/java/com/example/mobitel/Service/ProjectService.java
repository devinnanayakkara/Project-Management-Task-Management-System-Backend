package com.example.mobitel.Service;

import com.example.mobitel.Dto.ProjectDto;
import com.example.mobitel.Dto.ResponseDto;

public interface ProjectService {
     ResponseDto buildProject(ProjectDto request);

     ResponseDto seeProject(ProjectDto request);

     ResponseDto seeAllProjects();

     ResponseDto getPendingProjectsCount();

}
