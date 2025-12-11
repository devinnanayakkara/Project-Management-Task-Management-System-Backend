package com.example.mobitel.Controller;

import com.example.mobitel.Dto.LoginDto;
import com.example.mobitel.Dto.ResponseDto;
import com.example.mobitel.Dto.UserResponseDto;
import com.example.mobitel.Service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private LoginService loginService;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);



    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> UserLogin(@RequestBody Map<String, String> loginRequest) {

        String requestUserId = loginRequest.get("requestUserId");
        String requestUserPassword = loginRequest.get("requestUserPassword");

        try {
            UserResponseDto responseDto = loginService.loginUser(requestUserId, requestUserPassword);

            if ("Success".equalsIgnoreCase(responseDto.getStatus())) {

                logger.info("Successfully logged in for User: {}", requestUserId);
                return ResponseEntity.ok(responseDto);
            } else {

                logger.warn("Login failed for User: {} - {}", requestUserId, responseDto.getDescription());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
            }

        } catch (Exception e) {

            logger.error("Login unsuccessful for User: {}", requestUserId, e);
            UserResponseDto responseDto = new UserResponseDto();
            responseDto.setStatus("Failure");
            responseDto.setReturnCode("1003");
            responseDto.setDescription("Internal Server Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        }
    }


    @PostMapping("/adduser")
    public ResponseEntity<?> AddUser(@RequestBody LoginDto loginDto){
        try{
             ResponseDto responseUser = loginService.UserRegistration(loginDto);
            if ("Success".equalsIgnoreCase(responseUser.getStatus())) {

                return ResponseEntity.ok(responseUser);
            }else{
                 return ResponseEntity.status(400).body(responseUser);
            }
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal Server Error.");
        }
    }

    @GetMapping("/view")
    public ResponseEntity<?> viewUser() {
        try {
            ResponseDto response = loginService.viewUser();

            if ("Failure".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.status(400).body(response);
            } else if ("Error".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.status(500).body(response);
            }

            return ResponseEntity.status(200).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request while fetching users: {}", e.getMessage());
            ResponseDto errorResponse = new ResponseDto();
            errorResponse.setReturnCode("400");
            errorResponse.setStatus("Failure");
            errorResponse.setDescription(e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);

        } catch (Exception e) {
            logger.error("Failed to fetch users: {}", e.getMessage());
            ResponseDto errorResponse = new ResponseDto();
            errorResponse.setReturnCode("500");
            errorResponse.setStatus("Error");
            errorResponse.setDescription("Internal Server Error");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/view")
    public ResponseEntity<?> viewUserOneByOne(@RequestBody LoginDto loginDto) {
        try {
            // Call service to filter user by username,  and status
            LoginDto user = loginService.filterUser(loginDto);

            // If no user found
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("returnCode", "400");
                response.put("status", "Failure");
                response.put("description", "User not found or invalid input");
                return ResponseEntity.status(400).body(response);
            }

            // User found, return success response
            Map<String, Object> response = new HashMap<>();
            response.put("returnCode", "200");
            response.put("status", "Success");
            response.put("description", "User retrieved successfully");
            response.put("content", user);  // Return user details
            return ResponseEntity.status(200).body(response);

        } catch (Exception e) {
            logger.error("Failed to find user: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("returnCode", "500");
            response.put("status", "Error");
            response.put("description", "Internal Server Error");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getActiveUsersCount() {
        ResponseDto response;
        try {
            response = loginService.getActiveUsersCount();

            if ("Success".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else if ("Failure".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.status(404).body(response);
            } else {
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            logger.error("Failed to fetch active users count: {}", e.getMessage(), e);
            ResponseDto errorResponse = new ResponseDto();
            errorResponse.setReturnCode("500");
            errorResponse.setStatus("Error");
            errorResponse.setDescription("Internal Server Error");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }


    @GetMapping("/activeUser")
    public ResponseEntity<ResponseDto> getActiveUsers() {
        ResponseDto response = new ResponseDto();
        try {
            response = loginService.getAllEmployeesAndInterns();

            // Success response
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching active users: {}", e.getMessage(), e);

            response.setStatus("Error");
            response.setDescription("Internal Server Error while fetching active users.");
            response.setReturnCode("9999");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/unassigned-interns")
    public ResponseEntity<ResponseDto> getUnassignedInterns() {
        ResponseDto response = new ResponseDto();

        try {
            response = loginService.getUnassignedInterns();

            if ("SUCCESS".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            logger.error("Error while fetching unassigned interns: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal server error while fetching unassigned interns");
            response.setReturnCode("500");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/active-interns")
    public ResponseEntity<ResponseDto> getAllActiveInterns() {
        ResponseDto response = new ResponseDto();

        try {
            response = loginService.getAllActiveInterns();

            if ("SUCCESS".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            logger.error("Error while fetching active interns: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal server error while fetching active interns");
            response.setReturnCode("500");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/active-staff-tasks")
    public ResponseEntity<ResponseDto> getActiveStaffWithTasks() {
        ResponseDto response = new ResponseDto();

        try {
            response = loginService.getActiveStaffWithTasks();

            if ("SUCCESS".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }

        } catch (Exception e) {
            logger.error("Error while fetching active staff with tasks: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal server error while fetching active staff with tasks");
            response.setReturnCode("500");
            return ResponseEntity.status(500).body(response);
        }
    }



}








