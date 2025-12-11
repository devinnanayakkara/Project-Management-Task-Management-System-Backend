package com.example.mobitel.Controller;


import com.example.mobitel.Config.Utility.JwtUtil;
import com.example.mobitel.Dto.ProjectDto;
import com.example.mobitel.Dto.ResponseDto;
import com.example.mobitel.Dto.TaskDto;
import com.example.mobitel.Service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);



    @PostMapping("/create")
    public ResponseEntity<?> createTask(@RequestBody TaskDto taskDto) {
        try {
            ResponseDto response = taskService.createTask(taskDto);

            if ("Failure".equalsIgnoreCase(response.getStatus())) {

                return ResponseEntity.status(400).body(response);
            }

            return ResponseEntity.status(200).body(response);

        } catch (Exception e) {
            logger.error("Failed to create task: {}", e.getMessage());

            return ResponseEntity.status(500)
                    .body(Map.of("status", "Error", "message", "Internal Server Error"));
        }
    }

    @GetMapping("/view")
    public ResponseEntity<?> viewAllTasks() {
        try {
            ResponseDto response = taskService.viewTasks();

            if ("Failure".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.status(400).body(response);
            } else if ("Error".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.status(500).body(response);
            }

            return ResponseEntity.status(200).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request while fetching tasks: {}", e.getMessage());
            ResponseDto errorResponse = new ResponseDto();
            errorResponse.setReturnCode("400");
            errorResponse.setStatus("Failure");
            errorResponse.setDescription(e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);

        } catch (Exception e) {
            logger.error("Failed to find tasks: {}", e.getMessage());
            ResponseDto errorResponse = new ResponseDto();
            errorResponse.setReturnCode("500");
            errorResponse.setStatus("Error");
            errorResponse.setDescription("Internal Server Error");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }


    @PostMapping("/view")
    public ResponseEntity<?> viewTaskOneByOne(@RequestBody TaskDto taskDto) {
        try {
            TaskDto task = taskService.viewTheTaskOneByOne(taskDto);

            if (task == null) {

                Map<String, Object> response = new HashMap<>();
                response.put("returnCode", "400");
                response.put("status", "Failure");
                response.put("description", "Task not found or invalid input");
                return ResponseEntity.status(400).body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("returnCode", "200");
            response.put("status", "Success");
            response.put("description", "Task retrieved successfully");
            response.put("content", task);
            return ResponseEntity.status(200).body(response);

        } catch (Exception e) {
            logger.error("Failed to find task: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("returnCode", "500");
            response.put("status", "Error");
            response.put("description", "Internal Server Error");
            return ResponseEntity.status(500).body(response);
        }
    }


    @PostMapping("/update/{task_id}")
    public ResponseEntity<?> updateTask(@PathVariable("task_id") String taskId, @RequestBody TaskDto request) {
        try {
            //  set taskId inside DTO (if not already included)
            request.setTask_id(taskId);

            ResponseDto response = taskService.updateTask(request);

            if ("Failure".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.status(400).body(response);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to update task: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("status", "Error", "message", "Internal Server Error"));
        }
    }
    @GetMapping("/assignedOngoing")
    public ResponseEntity<?> assignedAndOngoingTasks(){
        try{
            ResponseDto tasks = taskService.getAssignedAndOngoingTasks();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Error for fetching assigned and ongoing tasks:{}",e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("status", "Error", "message", "Internal Server Error"));
        }
    }

    @GetMapping("/assignedOngoing/LoggedUserIntern/count")
    public ResponseEntity<?> getLoggedUserAndInternAssignedTaskCount(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            ResponseDto response = taskService.getLoggedUserAndInternAssignedTaskCount(token);

            if ("SUCCESS".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else if ("FAILURE".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.status(404).body(response);
            } else {
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("status", "ERROR", "message", "Internal Server Error"));
        }
    }


    @GetMapping("/assignedOngoing/count")
    public ResponseEntity<?> getAssignedOngoingTasksCount() {
        try {
            ResponseDto response = taskService.getAssignedOngoingTasksCount();

            if ("Success".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else if ("Failure".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.status(404).body(response);
            } else {
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            logger.error("Failed to fetch assigned and ongoing tasks count: {}", e.getMessage(), e);
            ResponseDto errorResponse = new ResponseDto();
            errorResponse.setReturnCode("500");
            errorResponse.setStatus("Error");
            errorResponse.setDescription("Internal Server Error");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }




    @PostMapping("/assign")
    public ResponseEntity<ResponseDto> assignTask(@RequestBody TaskDto request) {
        ResponseDto response = new ResponseDto();
        try {
            if (request.getProject_id() == null || request.getTask_id() == null || request.getAssign_to() == null) {
                response.setStatus("Failure");
                response.setDescription("Missing required fields: project_id, task_id, or assign_to");
                response.setReturnCode("1003");
                return ResponseEntity.badRequest().body(response);
            }

            ResponseDto result = taskService.allocateTask(request);

            if ("Failure".equalsIgnoreCase(result.getStatus())) {
                return ResponseEntity.status(400).body(result);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            response.setStatus("Error");
            response.setDescription("Internal Server Error: " + e.getMessage());
            response.setReturnCode("500");
            return ResponseEntity.internalServerError().body(response);
        }
    }


    //  Get unassigned tasks for a specific project
    @GetMapping("/unassigned/{project_id}")
    public ResponseEntity<ResponseDto> getUnassignedTasksByProject(@PathVariable("project_id") Integer project_id) {
        ResponseDto response = new ResponseDto();
        try {
            response = taskService.getUnassignedTasksByProject(project_id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setStatus("Error");
            response.setDescription("Error fetching unassigned tasks: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/complete/{task_id}")
    public ResponseEntity<ResponseDto> taskCompletion(@PathVariable("task_id") String task_id, @RequestBody TaskDto request) {

        ResponseDto response = new ResponseDto();
        try {
            request.setTask_id(task_id);
            response = taskService.taskCompletion(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setStatus("FAILURE");
            response.setDescription("Error while completing task.");
            response.setReturnCode("500");
            return ResponseEntity.status(500).body(response);
        }
    }


    @GetMapping("/tasks-overview")
    public ResponseEntity<ResponseDto> getTasksOverview(HttpServletRequest request) {
        ResponseDto response = new ResponseDto();

        try {
            // Extract username from JWT token
            String token = request.getHeader("Authorization").substring(7);
            String username = jwtUtil.extractUsername(token);

            response = taskService.getTasksOverviewForLoggedUser(username);

            if ("SUCCESS".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }

        } catch (Exception e) {
            logger.error("Error fetching tasks overview: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching tasks overview");
            response.setReturnCode("500");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/intern-task-projects")
    public ResponseEntity<ResponseDto> getProjectsForInternTasks(HttpServletRequest request) {
        ResponseDto response = new ResponseDto();

        try {
            String token = request.getHeader("Authorization").substring(7);
            String username = jwtUtil.extractUsername(token);

            response = taskService.getProjectsForInternTasks(username);

            if ("SUCCESS".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }

        } catch (Exception e) {
            logger.error("Error fetching intern task projects: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error");
            response.setReturnCode("500");
            return ResponseEntity.status(500).body(response);
        }
    }




    @GetMapping("/user-intern-tasks")
    public ResponseEntity<ResponseDto> getUserAndInternTasks(HttpServletRequest request) {
        ResponseDto response = new ResponseDto();

        try {
            // Extract username from JWT token
            String token = request.getHeader("Authorization").substring(7);
            String username = jwtUtil.extractUsername(token);

            response = taskService.getUserAndInternTasks(username);

            if ("SUCCESS".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }

        } catch (Exception e) {
            logger.error("Error fetching user and intern tasks: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching user and intern tasks");
            response.setReturnCode("500");
            return ResponseEntity.status(500).body(response);
        }
    }


    @GetMapping("/logged-user-tasks")
    public ResponseEntity<ResponseDto> getLoggedUserTasks(HttpServletRequest request) {
        ResponseDto response = new ResponseDto();

        try {
            // Extract username from JWT token
            String token = request.getHeader("Authorization").substring(7);
            String username = jwtUtil.extractUsername(token);

            response = taskService.getLoggedUserTasks(username);

            if ("SUCCESS".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }

        } catch (Exception e) {
            logger.error("Error fetching logged user tasks: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching logged user tasks");
            response.setReturnCode("500");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/logged-user-tasks/count")
    public ResponseEntity<ResponseDto> getLoggedUserTasksCount(HttpServletRequest request) {
        ResponseDto response = new ResponseDto();

        try {
            String token = request.getHeader("Authorization").substring(7);
            String username = jwtUtil.extractUsername(token);

            response = taskService.getLoggedUserTasksCount(username);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching logged user task count: {}", e.getMessage(), e);

            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching logged user task count");
            response.setReturnCode("500");

            return ResponseEntity.status(500).body(response);
        }
    }


    @GetMapping("/logged-user-tasks/completed/count")
    public ResponseEntity<ResponseDto> getLoggedUserCompletedTasksCount(HttpServletRequest request) {
        ResponseDto response = new ResponseDto();

        try {
            // Extract token → username
            String token = request.getHeader("Authorization").substring(7);
            String username = jwtUtil.extractUsername(token);

            response = taskService.getLoggedUserCompletedTasksCount(username);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching logged user completed task count: {}", e.getMessage(), e);

            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching logged user completed task count");
            response.setReturnCode("500");

            return ResponseEntity.status(500).body(response);
        }
    }
    @GetMapping("/logged-user-tasks/active/count")
    public ResponseEntity<ResponseDto> getLoggedUserActiveTasksCount(HttpServletRequest request) {
        ResponseDto response = new ResponseDto();

        try {
            // Extract token → username
            String token = request.getHeader("Authorization").substring(7);
            String username = jwtUtil.extractUsername(token);

            response = taskService.getLoggedUserActiveTasksCount(username);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching logged user active task count: {}", e.getMessage(), e);

            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching logged user active task count");
            response.setReturnCode("500");

            return ResponseEntity.status(500).body(response);
        }
    }


    @GetMapping("/user-intern-tasks/count")
    public ResponseEntity<ResponseDto> getUserInternAndUnassignedTaskCounts(HttpServletRequest request) {
        ResponseDto response = new ResponseDto();

        try {
            // Extract username from JWT
            String token = request.getHeader("Authorization").substring(7);
            String username = jwtUtil.extractUsername(token);

            response = taskService.getUserInternAndUnassignedTaskCounts(username);

            if ("SUCCESS".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }

        } catch (Exception e) {
            logger.error("Error fetching task counts: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching task counts");
            response.setReturnCode("500");
            return ResponseEntity.status(500).body(response);
        }
    }





    @PostMapping("/swap/{oldTaskId}")
    public ResponseEntity<ResponseDto> swapTask(@PathVariable("oldTaskId") String oldTaskId, @RequestBody TaskDto newTaskRequest) {

        ResponseDto response = new ResponseDto();
        try {
            response = taskService.swapTaskAndAssign(oldTaskId, newTaskRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setStatus("FAILURE");
            response.setDescription("Error while swapping the task.");
            response.setReturnCode("500");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/activeTasks")
    public ResponseEntity<?> getAllActiveTasks() {
        try {
            ResponseDto tasks = taskService.getActiveTasks();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Error fetching active tasks: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("status", "Error", "message", "Internal Server Error"));
        }
    }

    @GetMapping("/activeTasks/count")
    public ResponseEntity<?> getActiveTasksCount() {
        try {
            ResponseDto response = taskService.getActiveTasksCount();

            if ("Success".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else if ("Failure".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.status(404).body(response);
            } else {
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            logger.error("Failed to fetch active tasks count: {}", e.getMessage(), e);
            ResponseDto errorResponse = new ResponseDto();
            errorResponse.setReturnCode("500");
            errorResponse.setStatus("Error");
            errorResponse.setDescription("Internal Server Error");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/completedTasks")
    public ResponseEntity<?> getCompletedTasks() {
        try {
            ResponseDto response = taskService.getCompletedTasks();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching completed tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("status", "Error", "message", "Internal Server Error"));
        }
    }

    @GetMapping("/completedTasks/count")
    public ResponseEntity<?> getCompletedTasksCount() {
        try {
            ResponseDto response = taskService.getCompletedTasksCount();

            if ("Success".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else if ("Failure".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.status(404).body(response);
            } else {
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            logger.error("Failed to fetch completed tasks count: {}", e.getMessage(), e);
            ResponseDto errorResponse = new ResponseDto();
            errorResponse.setReturnCode("500");
            errorResponse.setStatus("Error");
            errorResponse.setDescription("Internal Server Error");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/activeTasks/interns")
    public ResponseEntity<?> getActiveTasksAssignedToInterns() {
        try {
            ResponseDto response = taskService.getActiveTasksAssignedToInterns();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching active tasks assigned to interns: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("status", "Error", "message", "Internal Server Error"));
        }
    }
    @GetMapping("/completedTasksAssignedToInterns")
    public ResponseEntity<?> getCompletedTasksAssignedToInterns() {
        try {
            ResponseDto response = taskService.getCompletedTasksAssignedToInterns();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching completed tasks assigned to interns: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("status", "Error", "message", "Internal Server Error"));
        }
    }



    @PostMapping("/onHold")
    public ResponseEntity<ResponseDto> putTaskOnHold(@RequestBody TaskDto request, HttpServletRequest httpRequest) {

        ResponseDto response = new ResponseDto();

        try {
            if (request.getTask_id() == null || request.getTask_id().isEmpty()) {
                response.setStatus("FAILURE");
                response.setDescription("Task ID is required");
                response.setReturnCode("1003");
                return ResponseEntity.badRequest().body(response);
            }

            // Extract username correctly
            String token = httpRequest.getHeader("Authorization").substring(7);
            String username = jwtUtil.extractUsername(token);

            request.setAdded_by(username);


            response = taskService.putTaskOnHold(request);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error putting task on-hold: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error");
            response.setReturnCode("500");
            return ResponseEntity.status(500).body(response);
        }
    }




    @PostMapping("/completed-projects")
    public ResponseEntity<ResponseDto> projectCompletion(@RequestBody TaskDto request) {
        try {
            ResponseDto response = taskService.checkAndUpdateProjectCompletion(request);


            if ("Failure".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.status(400).body(response);
            } else if ("Error".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.status(500).body(response);
            }

            // Default: success
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error completing project: {}", e.getMessage(), e);

            ResponseDto error = new ResponseDto();
            error.setReturnCode("500");
            error.setStatus("Error");
            error.setDescription("Internal server error: " + e.getMessage());

            return ResponseEntity.status(500).body(error);
        }
    }


}
