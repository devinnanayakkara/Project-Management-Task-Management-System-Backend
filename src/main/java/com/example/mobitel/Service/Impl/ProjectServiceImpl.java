package com.example.mobitel.Service.Impl;

import com.example.mobitel.Dao.Impl.ProjectDaoImpl;
import com.example.mobitel.Dto.ProjectDto;
import com.example.mobitel.Dto.ResponseDto;
import com.example.mobitel.Service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Autowired
    public ProjectDaoImpl projectDao;

    @Override
    public ResponseDto buildProject(ProjectDto request) {
          ResponseDto response = new ResponseDto();

        try{
            ProjectDto existingProject = projectDao.findProject(request.getProject_name(), request.getStatus());
            if(existingProject != null){
                response.setStatus("Failure");
                response.setReturnCode("1002");
                response.setDescription("The Project With Same Name Already Exist.");
                logger.warn("Duplicate Project detected: {}", request.getProject_name());
                return response;
            }

            boolean result = projectDao.AddProject(request) > 0;

            if(result){
                response.setStatus("Success");
                response.setDescription("Project Created Successfully.");
                response.setReturnCode("1000");
                logger.info("Project Created Successfully. {}",request.getProject_name());
            }else{
                response.setStatus("Failure");
                response.setDescription("Error For Creating Project.");
                response.setReturnCode("1001");
                logger.error("Failed to Create Project. {}", request.getProject_name());
            }
        } catch (Exception e) {
            logger.error("Error Creating Project. {}", e.getMessage());
            response.setStatus("Error");
            response.setDescription("Internal Server Error.");
            response.setReturnCode("500");

        }
        return response;
    }

    @Override
    public ResponseDto seeProject(ProjectDto request) {
        ResponseDto response = new ResponseDto();
        try {
            String projectName = request.getProject_name();
            String status = request.getStatus();

            // Validation
            if ((projectName == null || projectName.trim().isEmpty()) ||
                    (status == null || status.trim().isEmpty())) {
                response.setStatus("Failure");
                response.setReturnCode("400");
                response.setDescription("Project Name and Status cannot be null or empty.");
                return response;
            }

            ProjectDto project = projectDao.searchProject(projectName, status);

            if (project == null) {
                response.setStatus("Failure");
                response.setReturnCode("400");
                response.setDescription("No Project Found");
                return response;
            }

            // Success case
            response.setStatus("Success");
            response.setReturnCode("200");
            response.setDescription("Project retrieved Successfully.");
            response.setContent(project);

        } catch (Exception e) {
            logger.error("Error fetching project: {}", e.getMessage(), e);
            response.setReturnCode("500");
            response.setStatus("Error");
            response.setDescription("Error fetching Project: " + e.getMessage());
        }

        return response;
    }

    @Override
    public ResponseDto seeAllProjects() {
        ResponseDto response = new ResponseDto();
        try {
            // Fetch all projects from DAO
            List<ProjectDto> projectList = projectDao.viewProjects();

            if (projectList == null || projectList.isEmpty()) {
                response.setReturnCode("400");
                response.setStatus("Failure");
                response.setDescription("No projects found.");
            } else {
                response.setReturnCode("200");
                response.setStatus("Success");
                response.setDescription("Projects retrieved successfully.");
                response.setContent(projectList);
            }

        } catch (Exception e) {
            response.setReturnCode("500");
            response.setStatus("Error");
            response.setDescription("Error fetching all projects: " + e.getMessage());
        }
        return response;
    }

        @Override
        public ResponseDto getPendingProjectsCount() {
            ResponseDto response = new ResponseDto();
            try {
                int count = projectDao.countPendingProjects();

                if (count <= 0) {
                    response.setReturnCode("404");
                    response.setStatus("Failure");
                    response.setDescription("No pending projects found.");
                } else {
                    response.setReturnCode("200");
                    response.setStatus("Success");
                    response.setDescription("Pending projects count retrieved successfully.");
                    response.setContent(count);
                }
            } catch (Exception e) {
                response.setReturnCode("500");
                response.setStatus("Error");
                response.setDescription("Error fetching pending projects count: " + e.getMessage());
            }
            return response;
        }
    }


