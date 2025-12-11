package com.example.mobitel.Service;

import com.example.mobitel.Dto.ResponseDto;
import com.example.mobitel.Dto.TaskDto;
import com.example.mobitel.Dto.UserResponseDto;

import java.util.List;

public interface TaskService {
    ResponseDto createTask(TaskDto request);

    //List<TaskDto> viewTasks();

    TaskDto viewTheTaskOneByOne( TaskDto request);

    ResponseDto viewTasks();

    ResponseDto updateTask(TaskDto request);

    ResponseDto allocateTask(TaskDto request);

    ResponseDto getAssignedAndOngoingTasks();

    ResponseDto getUnassignedTasksByProject(Integer project_id);

    ResponseDto taskCompletion(TaskDto request);

    ResponseDto getTasksOverviewForLoggedUser(String Username);

    ResponseDto getUserAndInternTasks(String username);

    ResponseDto swapTaskAndAssign(String oldTaskId, TaskDto newTaskRequest);

    ResponseDto getActiveTasks();

    ResponseDto getCompletedTasks();

    ResponseDto getActiveTasksAssignedToInterns();

    ResponseDto getCompletedTasksAssignedToInterns();

    ResponseDto getLoggedUserTasks(String username);

    ResponseDto getAssignedOngoingTasksCount();

    ResponseDto getActiveTasksCount();

    ResponseDto getCompletedTasksCount();

    ResponseDto getUserInternAndUnassignedTaskCounts(String username);

    ResponseDto getLoggedUserTasksCount(String username);

    ResponseDto getLoggedUserAndInternAssignedTaskCount(String token);

    ResponseDto getLoggedUserCompletedTasksCount(String username);

    ResponseDto getLoggedUserActiveTasksCount(String username);

    ResponseDto putTaskOnHold(TaskDto request);

    ResponseDto getProjectsForInternTasks(String username);

    ResponseDto checkAndUpdateProjectCompletion(TaskDto request);






}
