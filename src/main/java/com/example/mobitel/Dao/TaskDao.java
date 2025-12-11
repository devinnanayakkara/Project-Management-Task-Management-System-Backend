package com.example.mobitel.Dao;


import com.example.mobitel.Dto.ProjectDto;
import com.example.mobitel.Dto.TaskDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TaskDao {
    int addTask(TaskDto request);

    TaskDto findTaskByName(String taskName);

    TaskDto findTaskByDescription(String taskDescription);
    List<TaskDto> viewAllTasks();

    TaskDto findTaskByNameAndStatus(String taskName, String status);

    int updateTask(TaskDto request);

   // TaskDto findAssignTask(String task_id, String assign_to);

     List<TaskDto> getAllAssignedAndOngoingTasks();

    int assignTask(TaskDto request);

    List<TaskDto> getUnassignedTasksByProject(Integer project_id);

    int completeTask(TaskDto request);

    Map<String, List<TaskDto>> getTasksOverviewForUser(String username);

    Map<String, List<TaskDto>> getUserAndInternTasks(String username);

    int terminateAndInsertTask(String oldTaskId, TaskDto newTaskRequest);

    List<TaskDto>getActiveTasks();

    List<TaskDto> getCompletedTasks();

    List<TaskDto> getActiveTasksAssignedToInterns();

    List<TaskDto> getCompletedTasksAssignedToInterns();

    List<TaskDto> getLoggedUserTasks(String username);

    int countAssignedAndOngoingTasks();

    int countActiveTasks();

    int countCompletedTasks();

    Map<String, Integer> getUserInternAndUnassignedTaskCounts(String username);

    int getLoggedUserTasksCount(String username);

    int getLoggedUserAndInternAssignedTaskCount(String username);

    int getLoggedUserCompletedTasksCount(String username);

    int getLoggedUserActiveTasksCount(String username);

    TaskDto findAssignTask(String taskId, String assignTo);

    void terminatePreviousTask(String taskId, String terminatedBy);

    boolean updateTaskStatusToOnHold(String taskId, String username);

    List<ProjectDto> getProjectsByIds(Set<Integer> projectIds);

    int getProjectIdByTask(String taskId);

    boolean areAllProjectTasksCompleted(int projectId);

    boolean markProjectAsCompleted(int projectId);

}
