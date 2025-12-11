package com.example.mobitel.Service;

import com.example.mobitel.Dto.LoginDto;
import com.example.mobitel.Dto.ResponseDto;
import com.example.mobitel.Dto.TaskDto;
import com.example.mobitel.Dto.UserResponseDto;
import org.apache.catalina.User;

import java.util.List;
import java.util.Map;

public interface LoginService {

    UserResponseDto loginUser(String userId, String password);

    ResponseDto UserRegistration(LoginDto request);

    //UserResponseDto UserLogging(String userId,String password,String userType);

   ResponseDto viewUser();

   LoginDto filterUser(LoginDto loginDto);

    ResponseDto getAllEmployeesAndInterns();

    ResponseDto getUnassignedInterns();

    ResponseDto getAllActiveInterns();

    ResponseDto getActiveStaffWithTasks();

    ResponseDto getActiveUsersCount();
}
