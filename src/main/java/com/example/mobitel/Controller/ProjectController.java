package com.example.mobitel.Controller;

import com.example.mobitel.Dto.ProjectDto;
import com.example.mobitel.Dto.ResponseDto;
import com.example.mobitel.Service.Impl.ProjectServiceImpl;
import com.example.mobitel.Service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    public ProjectServiceImpl projectService;

    @PostMapping("/create/project")
    public ResponseEntity<?> createProject(@RequestBody ProjectDto projectDto) {
        try {
            ResponseDto response = projectService.buildProject(projectDto);


            if ("Failure".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.status(400).body(response);
            }


            return ResponseEntity.status(200).body(response);

        } catch (Exception e) {
            logger.error("Failed to create project: {}", e.getMessage(), e);

            Map<String, Object> respond = new HashMap<>();
            respond.put("returnCode", "500");
            respond.put("status", "Error");
            respond.put("description", "Internal Server Error");
            return ResponseEntity.status(500).body(respond);
        }
    }
    @PostMapping("/view/projects")
    public ResponseEntity<?> viewProjects(@RequestBody ProjectDto projectDto) {
        try {
            ResponseDto response = projectService.seeProject(projectDto);

            if ("Failure".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.status(404).body(response);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to view projects: {}", e.getMessage(), e);

            Map<String, Object> respond = new HashMap<>();
            respond.put("returnCode", "500");
            respond.put("status", "Error");
            respond.put("description", "Internal Server Error");
            return ResponseEntity.status(500).body(respond);
        }
    }
    @GetMapping("/view/projects")
    public ResponseEntity<?> viewAllProjects() {
        ResponseDto response = new ResponseDto();
        try {
            // Call service method to get all pending projects
            ResponseDto projectsResponse = projectService.seeAllProjects();

            if ("Failure".equalsIgnoreCase(projectsResponse.getStatus()) ||
                    projectsResponse.getContent() == null ||
                    ((List<?>)projectsResponse.getContent()).isEmpty()) {
                response.setStatus("Failure");
                response.setReturnCode("404");
                response.setDescription("No pending projects found.");
                return ResponseEntity.status(404).body(response);
            }

            return ResponseEntity.ok(projectsResponse);

        } catch (Exception e) {
            logger.error("Failed to view all projects: {}", e.getMessage(), e);
            response.setStatus("Error");
            response.setReturnCode("500");
            response.setDescription("Internal Server Error");
            return ResponseEntity.status(500).body(response);
        }
    }
    @GetMapping("/pending/count")
    public ResponseEntity<?> getPendingProjectsCount() {
        ResponseDto response = new ResponseDto();
        try {
            ResponseDto countResponse = projectService.getPendingProjectsCount();

            if ("Failure".equalsIgnoreCase(countResponse.getStatus()) || countResponse.getContent() == null) {
                response.setStatus("Failure");
                response.setReturnCode("404");
                response.setDescription("No pending projects found.");
                return ResponseEntity.status(404).body(response);
            }

            return ResponseEntity.ok(countResponse);

        } catch (Exception e) {
            logger.error("Failed to get pending projects count: {}", e.getMessage(), e);
            response.setStatus("Error");
            response.setReturnCode("500");
            response.setDescription("Internal Server Error");
            return ResponseEntity.status(500).body(response);
        }
    }

}


