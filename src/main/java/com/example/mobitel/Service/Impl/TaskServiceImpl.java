package com.example.mobitel.Service.Impl;

import com.example.mobitel.Config.Utility.JwtUtil;
import com.example.mobitel.Dao.TaskDao;
import com.example.mobitel.Dto.ProjectDto;
import com.example.mobitel.Dto.ResponseDto;
import com.example.mobitel.Dto.TaskDto;
import com.example.mobitel.Service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Override
    public ResponseDto createTask(TaskDto request) {
        ResponseDto response = new ResponseDto();
        try {
                //check the task already exist or not
          //  TaskDto existingTask = taskDao.findTaskByName(request.getTask_name());
//            if (existingTask != null) {
//                response.setStatus("FAILURE");
//                response.setDescription("Task With the Same Name Already Exists.");
//                response.setReturnCode("1002");
//                logger.warn("Duplicate task detected: {}", request.getTask_name());
//                return response;
//            }


            boolean result = taskDao.addTask(request) > 0;

            if (result) {
                response.setStatus("SUCCESS");
                response.setDescription("Task Created Successfully.");
                response.setReturnCode("1000");
                logger.info("Task Created Successfully: {}", request.getTask_name());
            } else {
                response.setStatus("FAILURE");
                response.setDescription("Failed to create task.");
                response.setReturnCode("1001");
                logger.error("Failed to create task: {}", request.getTask_name());
            }

        } catch (Exception e) {
            logger.error("Error creating task: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error");
            response.setReturnCode("500");
        }

        return response;
    }


    @Override
    public ResponseDto viewTasks() {
        ResponseDto response = new ResponseDto();
        try {
            List<TaskDto> tasks = taskDao.viewAllTasks();

            if (tasks == null || tasks.isEmpty()) {
                response.setReturnCode("400");
                response.setStatus("Failure");
                response.setDescription("No tasks found.");
            } else {
                response.setReturnCode("200");
                response.setStatus("Success");
                response.setDescription("Tasks retrieved successfully.");
                response.setContent(tasks);
            }

        } catch (Exception e) {
            response.setReturnCode("500");
            response.setStatus("Error");
            response.setDescription("Error fetching all tasks: " + e.getMessage());
        }
        return response;
    }


    @Override
    public TaskDto viewTheTaskOneByOne(TaskDto taskDto) {
        try {
            String taskName = taskDto.getTask_name();
            String status = taskDto.getStatus();

            if (taskName == null || taskName.trim().isEmpty()) {
                logger.error("Task Name cannot be null or empty");
                return null;
            }

            if (status == null || status.trim().isEmpty()) {
                logger.error("Status cannot be null or empty.");
                return null;
            }

            taskName = taskName.trim();
            status = status.trim();

            TaskDto existingTask = taskDao.findTaskByNameAndStatus(taskName, status);

            if (existingTask == null) {
                logger.error("The task with name '{}' and status '{}' does not exist.", taskName, status);
            } else {
                logger.info("The task with name '{}' and status '{}' exist.", taskName, status);
            }

            return existingTask;

        } catch (Exception e) {
            throw new RuntimeException("Error checking Task creation.", e);
        }
    }



    @Override
    public ResponseDto updateTask(TaskDto request) {
        ResponseDto response = new ResponseDto();

        try {

            TaskDto existingTask = taskDao.findTaskByDescription(request.getTask_description());
            if (existingTask != null && !existingTask.getTask_id().equals(request.getTask_id())) {
                response.setStatus("FAILURE");
                response.setDescription("Another task with the same description already exists.");
                response.setReturnCode("1002");
                logger.warn("Duplicate task description detected during update: {}", request.getTask_description());
                return response;
            }


            int updatedRows = taskDao.updateTask(request);

            if (updatedRows > 0) {
                response.setStatus("SUCCESS");
                response.setDescription("Task updated successfully.");
                response.setReturnCode("1000");
                logger.info("Task updated successfully: {}", request.getTask_name());
            } else {
                response.setStatus("FAILURE");
                response.setDescription("Failed to update task. No records changed.");
                response.setReturnCode("1001");
                logger.error("Failed to update task: {}", request.getTask_name());
            }

        } catch (Exception e) {
            logger.error("Error updating task: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error");
            response.setReturnCode("500");
        }

        return response;
    }




    @Transactional
    @Override
    public ResponseDto allocateTask(TaskDto request) {
       ResponseDto response = new ResponseDto();

       try {
           // Validate
          if (request.getProject_id() == null || request.getTask_id() == null || request.getAssign_to() == null) {
            response.setStatus("Failure");
            response.setDescription("Missing required fields in request body");
            response.setReturnCode("1003");
            return response;
        }

        // Check if task is already assigned to someone
        TaskDto existing = taskDao.findAssignTask(request.getTask_id(), request.getAssign_to());
        if (existing != null) {
            response.setStatus("Failure");
            response.setDescription("This task is already assigned to the selected user.");
            response.setReturnCode("1002");
            return response;
        }

        // Terminate previous task assignment in tasks table
        taskDao.terminatePreviousTask(request.getTask_id(), request.getAdded_by());

        // Assign new task in both tables
        int rows = taskDao.assignTask(request);
        if (rows > 0) {
            response.setStatus("SUCCESS");
            response.setDescription("Task assigned successfully.");
            response.setReturnCode("1000");
        } else {
            response.setStatus("Failure");
            response.setDescription("Failed to assign task.");
            response.setReturnCode("1001");
        }

    } catch (Exception e) {
        response.setStatus("Error");
        response.setDescription("Internal Server Error: " + e.getMessage());
        response.setReturnCode("500");
        throw e; // ensures transaction rollback
    }

     return response;
}

    @Override
    public ResponseDto getAssignedAndOngoingTasks() {
        ResponseDto response = new ResponseDto();
        try{
         List<TaskDto> tasks = taskDao.getAllAssignedAndOngoingTasks();

         if(tasks.isEmpty()){
             response.setStatus("FAILURE");
             response.setDescription("No assigned or ongoing tasks found.");
             response.setReturnCode("1001");
         }else{
             response.setStatus("SUCCESS");
             response.setDescription("Assigned and Ongoing tasks fetched Successfully.");
             response.setReturnCode("1000");
             response.setContent(tasks);
         }

        }catch(Exception e){
            logger.error("Error for fetching tasks: {}", e.getMessage(),e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error");
            response.setReturnCode("500");
        }
        return response;
    }

    @Override
    public ResponseDto getUnassignedTasksByProject(Integer project_id) {
        ResponseDto response = new ResponseDto();

        try {
            List<TaskDto> taskList = taskDao.getUnassignedTasksByProject(project_id);

            if (taskList == null || taskList.isEmpty()) {
                response.setStatus("Success");
                response.setDescription("No unassigned tasks found for the selected project.");
                response.setContent(List.of());
            } else {
                response.setStatus("Success");
                response.setDescription("Unassigned tasks retrieved successfully.");
                response.setContent(taskList);
            }

        } catch (Exception e) {
            logger.error("Error retrieving unassigned tasks for project {}: {}", project_id, e.getMessage(), e);
            response.setStatus("Error");
            response.setDescription("Error retrieving unassigned tasks: " + e.getMessage());
            response.setContent(null);
        }

        return response;
    }

    @Override
    public ResponseDto taskCompletion(TaskDto request) {
        ResponseDto response = new ResponseDto();

        try {
            // Find task that is ACTIVE
            TaskDto existingTask = taskDao.findTaskByNameAndStatus(request.getTask_name(), "A");

            if (existingTask == null) {
                response.setStatus("FAILURE");
                response.setDescription("Task not found, already completed or on hold.");
                response.setReturnCode("1001");
                return response;
            }

            // Perform completion update
            int result = taskDao.completeTask(request);

            if (result > 0) {
                response.setStatus("SUCCESS");
                response.setDescription("Task marked as completed successfully.");
                response.setReturnCode("1000");
            } else {
                response.setStatus("FAILURE");
                response.setDescription("Failed to complete the task.");
                response.setReturnCode("1002");
            }

        } catch (Exception e) {
            logger.error("Error completing task: {}", request.getTask_name(), e);
            response.setStatus("FAILURE");
            response.setDescription("Internal Server Error.");
            response.setReturnCode("500");
        }

        return response;
    }

    @Override
    public ResponseDto getTasksOverviewForLoggedUser(String username) {
        ResponseDto response = new ResponseDto();

        try {
            Map<String, List<TaskDto>> tasks = taskDao.getTasksOverviewForUser(username);

            if ((tasks.get("userTasks") == null || tasks.get("userTasks").isEmpty()) &&
                    (tasks.get("unassignedTasks") == null || tasks.get("unassignedTasks").isEmpty()) &&
                    (tasks.get("internTasks") == null || tasks.get("internTasks").isEmpty())) {

                response.setStatus("FAILURE");
                response.setDescription("No tasks found for user or interns.");
                response.setReturnCode("1001");
            } else {
                response.setStatus("SUCCESS");
                response.setDescription("Tasks fetched successfully.");
                response.setReturnCode("1000");
                response.setContent(tasks);
            }

        } catch (Exception e) {
            logger.error("Error fetching tasks overview: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching tasks overview");
            response.setReturnCode("500");
        }

        return response;
    }




    @Override
    public ResponseDto getUserAndInternTasks(String username) {
        ResponseDto response = new ResponseDto();

        try {
            Map<String, List<TaskDto>> tasks = taskDao.getUserAndInternTasks(username);

            if ((tasks.get("userTasks") == null || tasks.get("userTasks").isEmpty()) &&
                    (tasks.get("internTasks") == null || tasks.get("internTasks").isEmpty())) {

                response.setStatus("FAILURE");
                response.setDescription("No tasks found for logged user or interns.");
                response.setReturnCode("1001");
            } else {
                response.setStatus("SUCCESS");
                response.setDescription("User and intern tasks fetched successfully.");
                response.setReturnCode("1000");
                response.setContent(tasks);
            }

        } catch (Exception e) {
            logger.error("Error fetching user and intern tasks: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching user and intern tasks");
            response.setReturnCode("500");
        }

        return response;
    }


    @Override
    public ResponseDto getLoggedUserTasks(String username) {
        ResponseDto response = new ResponseDto();

        try {
            List<TaskDto> tasks = taskDao.getLoggedUserTasks(username);

            if (tasks == null || tasks.isEmpty()) {
                response.setStatus("FAILURE");
                response.setDescription("No tasks found for logged user.");
                response.setReturnCode("1001");
            } else {
                response.setStatus("SUCCESS");
                response.setDescription("Logged user tasks fetched successfully.");
                response.setReturnCode("1000");
                response.setContent(tasks);
            }

        } catch (Exception e) {
            logger.error("Error fetching logged user tasks: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching logged user tasks");
            response.setReturnCode("500");
        }

        return response;
    }



    @Override
    @Transactional
    public ResponseDto swapTaskAndAssign(String oldTaskId, TaskDto newTaskRequest) {
        ResponseDto response = new ResponseDto();

        try {
            int result = taskDao.terminateAndInsertTask(oldTaskId, newTaskRequest);

            if (result > 0) {
                response.setStatus("SUCCESS");
                response.setDescription("Task swapped successfully — old task terminated and new task assigned.");
                response.setReturnCode("1000");
            } else {
                response.setStatus("FAILURE");
                response.setDescription("Task swap failed. No records affected.");
                response.setReturnCode("1001");
            }

        } catch (Exception e) {
            logger.error("Error swapping task: {}", e.getMessage(), e);
            response.setStatus("FAILURE");
            response.setDescription("Error while swapping task (rolled back).");
            response.setReturnCode("500");
            throw new RuntimeException("Swap failed — rolled back.", e);
        }

        return response;
    }
    @Override
    public ResponseDto getActiveTasks() {
        ResponseDto response = new ResponseDto();
        try {
            List<TaskDto> tasks = taskDao.getActiveTasks();

            if (tasks.isEmpty()) {
                response.setStatus("FAILURE");
                response.setDescription("No active tasks found.");
                response.setReturnCode("1001");
            } else {
                response.setStatus("SUCCESS");
                response.setDescription("Active tasks (assigned + unassigned) fetched successfully.");
                response.setReturnCode("1000");
                response.setContent(tasks);
            }

        } catch (Exception e) {
            logger.error("Error fetching active tasks: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error");
            response.setReturnCode("500");
        }
        return response;
    }
    @Override
    public ResponseDto getCompletedTasks() {
        ResponseDto response = new ResponseDto();
        try {
            List<TaskDto> tasks = taskDao.getCompletedTasks();

            if (tasks.isEmpty()) {
                response.setStatus("FAILURE");
                response.setDescription("No completed tasks found.");
                response.setReturnCode("1001");
            } else {
                response.setStatus("SUCCESS");
                response.setDescription("Completed tasks fetched successfully.");
                response.setReturnCode("1000");
                response.setContent(tasks);
            }
        } catch (Exception e) {
            logger.error("Error fetching completed tasks: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error");
            response.setReturnCode("500");
        }
        return response;
    }




    @Override
    public ResponseDto getCompletedTasksCount() {
        ResponseDto response = new ResponseDto();
        try {
            int count = taskDao.countCompletedTasks();
            response.setReturnCode("200");
            response.setStatus("Success");
            response.setDescription("Completed tasks count retrieved successfully.");
            response.setContent(count);
        } catch (Exception e) {
            logger.error("Error fetching completed tasks count: {}", e.getMessage(), e);
            response.setReturnCode("500");
            response.setStatus("Error");
            response.setDescription("Internal Server Error");
        }
        return response;
    }

    @Override
    public ResponseDto getActiveTasksAssignedToInterns() {
        ResponseDto response = new ResponseDto();
        try {
            List<TaskDto> tasks = taskDao.getActiveTasksAssignedToInterns();

            if (tasks.isEmpty()) {
                response.setStatus("FAILURE");
                response.setDescription("No active tasks assigned to interns found.");
                response.setReturnCode("1001");
            } else {
                response.setStatus("SUCCESS");
                response.setDescription("Active tasks assigned to interns fetched successfully.");
                response.setReturnCode("1000");
                response.setContent(tasks);
            }
        } catch (Exception e) {
            logger.error("Error fetching active tasks assigned to interns: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error");
            response.setReturnCode("500");
        }
        return response;
    }

    @Override
    public ResponseDto getCompletedTasksAssignedToInterns() {
        ResponseDto response = new ResponseDto();
        try {
            List<TaskDto> tasks = taskDao.getCompletedTasksAssignedToInterns();

            if (tasks.isEmpty()) {
                response.setStatus("FAILURE");
                response.setDescription("No completed tasks assigned to interns found.");
                response.setReturnCode("1001");
            } else {
                response.setStatus("SUCCESS");
                response.setDescription("Completed tasks assigned to interns fetched successfully.");
                response.setReturnCode("1000");
                response.setContent(tasks);
            }
        } catch (Exception e) {
            logger.error("Error fetching completed tasks assigned to interns: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error");
            response.setReturnCode("500");
        }
        return response;
    }

    @Override
    public ResponseDto getAssignedOngoingTasksCount() {
        ResponseDto response = new ResponseDto();
        try {
            int count = taskDao.countAssignedAndOngoingTasks();
            response.setReturnCode("200");
            response.setStatus("Success");
            response.setDescription("Assigned and ongoing tasks count retrieved successfully.");
            response.setContent(count);
        } catch (Exception e) {
            logger.error("Error fetching assigned and ongoing tasks count: {}", e.getMessage(), e);
            response.setReturnCode("500");
            response.setStatus("Error");
            response.setDescription("Internal Server Error");
        }
        return response;
    }
    @Override
    public ResponseDto getActiveTasksCount() {
        ResponseDto response = new ResponseDto();
        try {
            int count = taskDao.countActiveTasks();
            response.setReturnCode("200");
            response.setStatus("Success");
            response.setDescription("Active tasks count retrieved successfully.");
            response.setContent(count);
        } catch (Exception e) {
            logger.error("Error fetching active tasks count: {}", e.getMessage(), e);
            response.setReturnCode("500");
            response.setStatus("Error");
            response.setDescription("Internal Server Error");
        }
        return response;
    }


    @Override
    public ResponseDto getUserInternAndUnassignedTaskCounts(String username) {
        ResponseDto response = new ResponseDto();

        try {
            Map<String, Integer> counts = taskDao.getUserInternAndUnassignedTaskCounts(username);

            int userCount = counts.get("userTaskCount");
            int internCount = counts.get("internTaskCount");
            int unassignedCount = counts.get("unassignedTaskCount");

            if (userCount == 0 && internCount == 0 && unassignedCount == 0) {
                response.setStatus("FAILURE");
                response.setDescription("No tasks found.");
                response.setReturnCode("1001");
            } else {
                response.setStatus("SUCCESS");
                response.setDescription("Task counts successfully fetched");
                response.setReturnCode("1000");
                response.setContent(counts);
            }

        } catch (Exception e) {
            logger.error("Error fetching task counts: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching task counts");
            response.setReturnCode("500");
        }

        return response;
    }



    @Override
    public ResponseDto getLoggedUserAndInternAssignedTaskCount(String token) {
        ResponseDto response = new ResponseDto();
        try {
            // Extract username from JWT token
            String username = jwtUtil.extractUsername(token);

            int count = taskDao.getLoggedUserAndInternAssignedTaskCount(username);

            response.setStatus("SUCCESS");
            response.setReturnCode("1000");
            response.setDescription("Logged user + intern assigned task count fetched successfully.");
            response.setContent(count);

        } catch (Exception e) {
            logger.error("Error fetching task count: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setReturnCode("500");
            response.setDescription("Internal Server Error");
        }
        return response;
    }
    @Override
    public ResponseDto getLoggedUserTasksCount(String username) {
        ResponseDto response = new ResponseDto();

        try {
            int count = taskDao.getLoggedUserTasksCount(username);

            response.setStatus("SUCCESS");
            response.setDescription("Logged user task count fetched successfully.");
            response.setReturnCode("1000");
            response.setContent(count);

        } catch (Exception e) {
            logger.error("Error fetching logged user task count: {}", e.getMessage(), e);

            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching logged user task count");
            response.setReturnCode("500");
        }

        return response;
    }
    @Override
    public ResponseDto getLoggedUserCompletedTasksCount(String username) {
        ResponseDto response = new ResponseDto();

        try {
            int count = taskDao.getLoggedUserCompletedTasksCount(username);

            response.setStatus("SUCCESS");
            response.setDescription("Logged user completed task count fetched successfully.");
            response.setReturnCode("1000");
            response.setContent(count);

        } catch (Exception e) {
            logger.error("Error fetching logged user completed task count: {}", e.getMessage(), e);

            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching logged user completed task count");
            response.setReturnCode("500");
        }

        return response;
    }
    @Override
    public ResponseDto getLoggedUserActiveTasksCount(String username) {
        ResponseDto response = new ResponseDto();

        try {
            int count = taskDao.getLoggedUserActiveTasksCount(username);

            response.setStatus("SUCCESS");
            response.setDescription("Logged user active task count fetched successfully.");
            response.setReturnCode("1000");
            response.setContent(count);

        } catch (Exception e) {
            logger.error("Error fetching logged user active task count: {}", e.getMessage(), e);

            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching logged user active task count");
            response.setReturnCode("500");
        }

        return response;
    }

    @Override
    public ResponseDto putTaskOnHold(TaskDto request) {
        ResponseDto response = new ResponseDto();
        try {
            boolean updated = taskDao.updateTaskStatusToOnHold(
                    request.getTask_id(),
                    request.getAdded_by()
            );

            if (updated) {
                response.setStatus("SUCCESS");
                response.setDescription("Task put on-hold successfully.");
                response.setReturnCode("1000");
            } else {
                response.setStatus("FAILURE");
                response.setDescription("No task found to update.");
                response.setReturnCode("1001");
            }

        } catch (Exception e) {
            logger.error("Error putting task on-hold: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error");
            response.setReturnCode("500");
        }
        return response;
    }


    @Override
    public ResponseDto getProjectsForInternTasks(String username) {
        ResponseDto response = new ResponseDto();

        try {
            // Load the full task map for user + interns
            Map<String, List<TaskDto>> tasks = taskDao.getTasksOverviewForUser(username);

            List<TaskDto> internTasks = tasks.get("internTasks");

            if (internTasks == null || internTasks.isEmpty()) {
                response.setStatus("FAILURE");
                response.setDescription("No intern tasks found.");
                response.setReturnCode("1001");
                return response;
            }

            // Collect unique project IDs
            Set<Integer> projectIds = internTasks.stream()
                    .map(TaskDto::getProject_id)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // Fetch project records
            List<ProjectDto> projectList = taskDao.getProjectsByIds(projectIds);

            //  ADD TASK LIST PER PROJECT
            Map<Integer, List<Map<String, Object>>> projectTaskMap = internTasks.stream()
                    .collect(Collectors.groupingBy(
                            TaskDto::getProject_id,
                            Collectors.mapping(task -> {
                                Map<String, Object> taskInfo = new HashMap<>();
                                taskInfo.put("task_id", task.getTask_id());
                                taskInfo.put("task_name", task.getTask_name());
                                return taskInfo;
                            }, Collectors.toList())
                    ));

            // Attach tasks to each project
            List<Map<String, Object>> responseList = new ArrayList<>();

            for (ProjectDto p : projectList) {
                Map<String, Object> projectMap = new HashMap<>();

                projectMap.put("project_id", p.getProject_id());
                projectMap.put("project_name", p.getProject_name());
                projectMap.put("start_date", p.getStart_date());
                projectMap.put("expected_end_date", p.getExpected_end_date());

                projectMap.put("tasks", projectTaskMap.getOrDefault(
                        p.getProject_id(), new ArrayList<>()
                ));

                responseList.add(projectMap);
            }

            response.setStatus("SUCCESS");
            response.setDescription("Projects fetched successfully.");
            response.setReturnCode("1000");
            response.setContent(responseList);

        } catch (Exception e) {
            logger.error("Error fetching intern task projects: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error");
            response.setReturnCode("500");
        }

        return response;
    }

    @Override
    public ResponseDto checkAndUpdateProjectCompletion(TaskDto request) {
        ResponseDto response = new ResponseDto();

        try {
            int projectId = taskDao.getProjectIdByTask(request.getTask_id());
            if (projectId == 0) {
                response.setReturnCode("400");
                response.setStatus("Failure");
                response.setDescription("Invalid task ID");
                return response;
            }

            boolean allCompleted = taskDao.areAllProjectTasksCompleted(projectId);

            if (allCompleted) {
                boolean updated = taskDao.markProjectAsCompleted(projectId);

                if (!updated) {
                    response.setReturnCode("500");
                    response.setStatus("Error");
                    response.setDescription("Cannot update project status");
                    return response;
                }

                response.setReturnCode("200");
                response.setStatus("Success");
                response.setDescription("Project marked as Completed.");
            } else {
                response.setReturnCode("200");
                response.setStatus("Success");
                response.setDescription("Task completed. Project still has pending tasks.");
            }

        } catch (Exception e) {
            logger.error("Error during project completion check: {}", e.getMessage(), e);
            response.setReturnCode("500");
            response.setStatus("Error");
            response.setDescription("Error during project completion check: " + e.getMessage());
        }

        return response;
    }

}
