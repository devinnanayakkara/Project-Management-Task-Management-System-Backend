package com.example.mobitel.Service.Impl;

import com.example.mobitel.Config.Utility.JwtUtil;
import com.example.mobitel.Dao.LoginDao;
import com.example.mobitel.Dao.UserDao;
import com.example.mobitel.Dto.LoginDto;
import com.example.mobitel.Dto.ResponseDto;
import com.example.mobitel.Dto.UserResponseDto;
import com.example.mobitel.Properties.GlobalProperties;
import com.example.mobitel.Service.LoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GlobalProperties globalProperties;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LoginDao loginDao;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDao userDao;

    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);


    @Override
    public UserResponseDto loginUser(String userId, String password) {
        UserResponseDto userResponseDto = new UserResponseDto();

        try {
            String userInformationUrl = globalProperties.getLDapUrl();

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("requestUserId", userId);
            requestBody.put("requestUserPassword", password);
            requestBody.put("employeePhoto", "false");
            requestBody.put("appCode", globalProperties.getAppCode());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    userInformationUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String body = responseEntity.getBody();
                logger.info("Raw LDAP Response for user {}: {}", userId, body);

                JSONObject jsonObject = new JSONObject(body);
                String apiStatus = jsonObject.optString("status", "");
                String returnCode = jsonObject.optString("returnCode", "");
                boolean apiSuccess = jsonObject.optBoolean("success", false);

                boolean isSuccess =
                        "Success".equalsIgnoreCase(apiStatus) ||
                                "successful".equalsIgnoreCase(apiStatus) ||
                                "00".equals(returnCode) ||
                                apiSuccess;

                if (isSuccess) {
                    logger.info("Login Successful for user: {}", userId);

                    JSONObject userInfo = jsonObject.optJSONObject("userInformation");

                    // Fetch userType from DB instead of title
                    String role = "Employee"; // default role
                    try {
                        LoginDto dbUser = loginDao.findUserByUsername(userId);
                        if (dbUser != null && dbUser.getUserType() != null) {
                            role = dbUser.getUserType();
                            logger.info("Fetched userType '{}' from DB for user {}", role, userId);
                        } else {
                            logger.warn("UserType not found in DB for user: {}. Using default role '{}'", userId, role);
                        }
                    } catch (Exception dbEx) {
                        logger.error("Database error while fetching userType for user {}: {}", userId, dbEx.getMessage(), dbEx);
                    }

                    // Prepare Response DTO
                    userResponseDto.setStatus("Success");
                    userResponseDto.setReturnCode("1000");
                    userResponseDto.setDescription("Login Successful");
                    userResponseDto.setUserType(role);

                    // Generate JWT
//                    String jwtToken = jwtUtil.generateToken(userId, role);
//                    userResponseDto.setToken(jwtToken);

                    String systemUserId = null;
                    try {
                        LoginDto dbUser = loginDao.findUserByUsername(userId);
                        if (dbUser != null) {
                            systemUserId = dbUser.getUser_id(); // keep "USR006"
                        }
                    } catch (Exception e) {
                        logger.error("Error fetching system user ID for {}", userId, e);
                    }

                    // Generate JWT
                    String jwtToken = jwtUtil.generateToken(userId, role, systemUserId);
                    userResponseDto.setToken(jwtToken);




                    if (userInfo != null) {
                        Map<String, Object> userMap = new ObjectMapper().readValue(userInfo.toString(), Map.class);
                        userResponseDto.setContent(List.of(userMap));
                    } else {
                        userResponseDto.setContent(Collections.emptyList());
                    }

                } else {
                    String description = jsonObject.optString("description",
                            jsonObject.optString("message", "Invalid credentials"));
                    logger.warn("Login Failed for user {} - {}", userId, description);

                    userResponseDto.setStatus("Failure");
                    userResponseDto.setReturnCode("1001");
                    userResponseDto.setDescription(description);
                    userResponseDto.setUserType(null);
                    userResponseDto.setToken(null);
                    userResponseDto.setContent(null);
                }

            } else {
                logger.error("Login Failed for user {} - HTTP Status: {}", userId, responseEntity.getStatusCode());
                userResponseDto.setStatus("Failure");
                userResponseDto.setReturnCode("1002");
                userResponseDto.setDescription("Login Failed due to server error");
                userResponseDto.setUserType(null);
                userResponseDto.setToken(null);
                userResponseDto.setContent(null);
            }

        } catch (Exception e) {
            logger.error("Login Failed for user {} due to internal error", userId, e);
            userResponseDto.setStatus("Failure");
            userResponseDto.setReturnCode("1003");
            userResponseDto.setDescription("Login Failed due to internal error");
            userResponseDto.setUserType(null);
            userResponseDto.setToken(null);
            userResponseDto.setContent(null);
        }

        return userResponseDto;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDto UserRegistration(LoginDto request) {
        try {
            //  Basic validation
            if (request.getRequestUserId() == null || request.getRequestUserId().isEmpty()) {
                return buildResponse("Failure", "1003", "Username is required.");
            }

            //  Common setup
            request.setRoleCode("PMATM");
            request.setAdded_on(LocalDateTime.now().toLocalDate());
            String addedBy = (request.getAdded_by() != null && !request.getAdded_by().isEmpty())
                    ? request.getAdded_by()
                    : "System";
            request.setAdded_by(addedBy);


            String lastUserId = loginDao.getLastUserId(); // might return null if table is empty
            int nextNumber = 1;
            if (lastUserId != null && lastUserId.startsWith("USR")) {
                nextNumber = Integer.parseInt(lastUserId.substring(3)) + 1;
            }
            String newUserId = String.format("USR%03d", nextNumber); // USR001 if first user
            request.setUser_id(newUserId);


            if ("Intern".equalsIgnoreCase(request.getUserType())) {

                // Check duplicates in both tables
                if (userDao.findUserByUsername(request.getRequestUserId()) != null ||
                        loginDao.findUserByUsername(request.getRequestUserId()) != null) {
                    return buildResponse("Failure", "1002", "Intern already exists.");
                }

                request.setRequestUserPassword(null); // Interns have no password

                // Insert into PMATM
                int rowsInserted = loginDao.AddUser(request);
                if (rowsInserted <= 0) throw new RuntimeException("Failed to insert into PMATM.");

                // Insert into user_details using same user_id
                int addedToUserDetails = userDao.addUserToUserDetails(request);
                if (addedToUserDetails <= 0) throw new RuntimeException("Failed to insert into user_details.");

                return buildResponse("Success", "1000", "Intern registered successfully in both tables.");
            }


            else if ("Employee".equalsIgnoreCase(request.getUserType())) {

                if (loginDao.findUserByUsername(request.getRequestUserId()) != null) {
                    return buildResponse("Failure", "1002", "Employee already exists.");
                }

                request.setRequestUserPassword(null);

                // Insert into PMATM
                int rowsInserted = loginDao.AddUser(request);
                if (rowsInserted <= 0) throw new RuntimeException("Failed to insert Employee into PMATM.");

                // Optional: insert into user_details
                // int addedToUserDetails = userDao.addUserToUserDetails(request);
                // if (addedToUserDetails <= 0) throw new RuntimeException("Failed to insert into user_details.");

                return buildResponse("Success", "1000", "Employee registered successfully in PMATM table.");
            }

            //  INVALID TYPE
            else {
                return buildResponse("Failure", "1004", "Invalid user type. Only 'Intern' or 'Employee' allowed.");
            }

        } catch (DuplicateKeyException e) {
            logger.warn("Duplicate user detected: {}", e.getMessage());
            return buildResponse("Failure", "1002", "User already exists with the same username.");
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            return buildResponse("Failure", "1005", "User registration failed due to internal error.");
        }
    }


    @Override
    public ResponseDto viewUser() {

        ResponseDto Response = new ResponseDto();

        try{
            List<LoginDto> users = loginDao.viewAllUsers();

            if(users == null || users.isEmpty()){
                Response.setReturnCode("400");
                Response.setStatus("Failure");
                Response.setDescription("No users found.");

            }else{
                Response.setReturnCode("200");
                Response.setStatus("Success");
                Response.setDescription("Users retrieved successfully.");
                Response.setContent(users);
            }
        } catch (Exception e) {
            Response.setReturnCode("500");
            Response.setStatus("Error");
            Response.setDescription("Error fetching all tasks: " + e.getMessage());
        }

        return Response;
    }

    @Override
    public LoginDto filterUser(LoginDto loginDto) {
        try {
            String roleCode = loginDto.getRoleCode();
            String username = loginDto.getRequestUserId();
            String status = loginDto.getStatus();

            // Input validation: at least one parameter must be provided
            if ((roleCode == null || roleCode.trim().isEmpty()) &&
                    (username == null || username.trim().isEmpty()) &&
                    (status == null || status.trim().isEmpty())) {
                logger.error("At least one filter parameter (roleCode, username, or status) must be provided.");
                return null;
            }

            // Clean up input strings
            if (roleCode != null) roleCode = roleCode.trim();
            if (username != null) username = username.trim();
            if (status != null) status = status.trim();

            // Call DAO method to fetch user by username, roleCode, and status
            LoginDto filteredUser = loginDao.fetchUser( roleCode,username, status);

            if (filteredUser == null) {
                logger.warn("No user found for given filters - username: {}, roleCode: {}, status: {}",
                        username, roleCode, status);
            } else {
                logger.info("Filtered user found: {}", filteredUser.getRequestUserId());
            }

            return filteredUser;

        } catch (Exception e) {
            logger.error("Error filtering users: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public ResponseDto getAllEmployeesAndInterns() {
        ResponseDto response = new ResponseDto();
        try {
            List<Map<String,Object>>activeUsers = loginDao.getActiveEmployeesAndInterns();

            if (activeUsers.isEmpty()) {
                response.setStatus("Failure");
                response.setDescription("No active users found with roles Employee or Intern.");
                response.setReturnCode("1001");
                logger.warn("No active users found for Employee or Intern roles.");
            } else {
                response.setStatus("Success");
                response.setDescription("Active users fetched successfully.");
                response.setReturnCode("1000");
                response.setContent(activeUsers);
                logger.info("Fetched {} active users successfully.", activeUsers.size());
            }

        } catch (Exception e) {
            logger.error("Error in getAllEmployeesAndInterns: {}", e.getMessage(), e);
            response.setStatus("Error");
            response.setDescription("An error occurred while retrieving active users.");
            response.setReturnCode("9999");
        }
        return response;
    }

    @Override
    public ResponseDto getUnassignedInterns() {
        ResponseDto response = new ResponseDto();

        try {
            List<Map<String, Object>> interns = loginDao.getUnassignedInterns();

            if (interns.isEmpty()) {
                response.setStatus("FAILURE");
                response.setDescription("No unassigned interns found");
                response.setReturnCode("1001");
            } else {
                response.setStatus("SUCCESS");
                response.setDescription("Unassigned interns fetched successfully");
                response.setReturnCode("1000");
                response.setContent(interns);
            }

        } catch (Exception e) {
            logger.error("Error fetching unassigned interns: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal server error");
            response.setReturnCode("500");
        }

        return response;
    }



    @Override
    public ResponseDto getAllActiveInterns() {
        ResponseDto response = new ResponseDto();

        try {
            List<Map<String, Object>> interns = loginDao.getAllActiveInterns();

            if (interns.isEmpty()) {
                response.setStatus("FAILURE");
                response.setDescription("No active interns found");
                response.setReturnCode("1001");
            } else {
                response.setStatus("SUCCESS");
                response.setDescription("Active interns fetched successfully");
                response.setReturnCode("1000");
                response.setContent(interns);
            }

        } catch (Exception e) {
            logger.error("Error fetching active interns: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal server error while fetching active interns");
            response.setReturnCode("500");
        }

        return response;
    }

    @Override
    public ResponseDto getActiveStaffWithTasks() {
        ResponseDto response = new ResponseDto();

        try {
            List<Map<String, Object>> staffList = loginDao.getActiveStaffWithTasks();

            if (staffList.isEmpty()) {
                response.setStatus("FAILURE");
                response.setDescription("No active employees or interns found");
                response.setReturnCode("1001");
            } else {
                response.setStatus("SUCCESS");
                response.setDescription("Active employees and interns fetched successfully");
                response.setReturnCode("1000");
                response.setContent(staffList);
            }

        } catch (Exception e) {
            logger.error("Error fetching active staff with tasks: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal server error while fetching active staff with tasks");
            response.setReturnCode("500");
        }

        return response;
    }


    private ResponseDto buildResponse(String status, String returnCode, String description) {
        ResponseDto response = new ResponseDto();
        response.setStatus(status);
        response.setReturnCode(returnCode);
        response.setDescription(description);

        return response;
    }

    @Override
    public ResponseDto getActiveUsersCount() {
        ResponseDto response = new ResponseDto();
        try {
            int userCount = loginDao.countActiveUsers();

            response.setReturnCode("200");
            response.setStatus("Success");
            response.setDescription("Active users count retrieved successfully.");
            response.setContent(userCount);

        } catch (Exception e) {
            response.setReturnCode("500");
            response.setStatus("Error");
            response.setDescription("Error fetching active users count: " + e.getMessage());
        }
        return response;
    }



}

