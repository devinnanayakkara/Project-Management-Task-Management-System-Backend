package com.example.mobitel.Dao;

import com.example.mobitel.Dto.LoginDto;
import com.example.mobitel.Dto.UserResponseDto;

import java.util.List;
import java.util.Map;

public interface LoginDao {

   //LoginDto findUserByUserNameAndRoleCode(String requestUserId,String roleCode);

   int AddUser(LoginDto loginDto);

   LoginDto findUserByUsername(String username);

   LoginDto findUserByUsernameOrEmail(String username, String email);

   List<LoginDto> viewAllUsers();

   LoginDto fetchUser(String roleCode,String username, String status);

   String getLastUserId();

   List<Map<String,Object>> getActiveEmployeesAndInterns();

   List<Map<String, Object>> getUnassignedInterns();

   List<Map<String,Object>> getAllActiveInterns();

   List<Map<String, Object>>  getActiveStaffWithTasks();

   int countActiveUsers();

}
