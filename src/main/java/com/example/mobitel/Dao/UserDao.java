package com.example.mobitel.Dao;


import com.example.mobitel.Dto.LoginDto;
import com.example.mobitel.Dto.ProjectDto;
import com.example.mobitel.Dto.UserDto;

import java.util.List;

public interface UserDao {

         // Fetch user by email


         // Save new user to database




        LoginDto findUserByUsername(String username);

        int addUserToUserDetails(LoginDto loginDto);

        LoginDto findUserByUsernameOrEmail(String username, String email);

}
