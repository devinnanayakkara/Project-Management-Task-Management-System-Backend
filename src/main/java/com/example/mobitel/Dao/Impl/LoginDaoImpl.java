package com.example.mobitel.Dao.Impl;

import com.example.mobitel.Dao.LoginDao;
import com.example.mobitel.Dao.TaskDao;
import com.example.mobitel.Dto.LoginDto;
import com.example.mobitel.Dto.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class LoginDaoImpl implements LoginDao {

    private static final Logger logger = LoggerFactory.getLogger(LoginDaoImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;



    @Override
    public int AddUser(LoginDto loginDto) {
        String sql = "INSERT INTO user_management.pmatm " +
                "(user_id,roleCode,userType,username,status,added_by,added_on) " +
                "VALUES( ?, ?,?, ?, ?, ?, ?)";

        try {
            return jdbcTemplate.update(sql,
                    loginDto.getUser_id(),
                    loginDto.getRoleCode(),
                    loginDto.getUserType(),
                    loginDto.getRequestUserId(),
                    "A",
                    loginDto.getAdded_by(),
                    loginDto.getAdded_on()
            );
        } catch (DuplicateKeyException e) {
            logger.warn("Duplicate entry in PMATM: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Cannot Add the user to PMATM: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add user to PMATM", e);
        }
    }


    @Override
    public LoginDto findUserByUsername(String username) {
        String sql = "SELECT * FROM user_management.pmatm WHERE username = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(LoginDto.class), username);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public LoginDto findUserByUsernameOrEmail(String username, String email) {
        String sql = "SELECT * FROM user_management.pmatm WHERE username = ? OR email = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(LoginDto.class), username, email);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<LoginDto> viewAllUsers() {
        String sql = "SELECT * " +
                "FROM pmatm " +
                "WHERE roleCode = 'PMATM' " +
                "AND terminated_on IS NULL " +
                "AND terminated_by IS NULL;";

        List<LoginDto> users = new ArrayList<>();
           try{
               SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
               while(sqlRowSet.next()){
                   LoginDto loginDto = new LoginDto();
                   loginDto.setUser_id(sqlRowSet.getString("user_id"));
                   loginDto.setRoleCode(sqlRowSet.getString("roleCode"));
                   loginDto.setUserType(sqlRowSet.getString("userType"));
                   loginDto.setRequestUserId(sqlRowSet.getString("username"));
                   loginDto.setStatus(sqlRowSet.getString("status"));
                   loginDto.setAdded_by(sqlRowSet.getString("added_by"));
                   loginDto.setAdded_on(sqlRowSet.getDate("added_on").toLocalDate());

                   users.add(loginDto);
               }

           } catch (Exception e) {
               logger.error("cannot find users", e);
           }

           return users;
    }



    @Override
    public LoginDto fetchUser(String roleCode, String username, String status) {

        String sql = "SELECT user_id, roleCode, userType, username, status, added_by, added_on " +
                "FROM pmatm " +
                "WHERE roleCode = ? " +
                "AND username = ? " +
                "AND status = ? " +
                "AND terminated_on IS NULL " +
                "AND terminated_by IS NULL;";

        LoginDto loginDto = null;

        try {
            SqlRowSet sqlrowSet = jdbcTemplate.queryForRowSet(sql, roleCode, username, status);
            if (sqlrowSet.next()) {
                loginDto = new LoginDto();

                loginDto.setUser_id(sqlrowSet.getString("user_id"));
                loginDto.setRoleCode(sqlrowSet.getString("roleCode"));
                loginDto.setUserType(sqlrowSet.getString("userType"));
                loginDto.setRequestUserId(sqlrowSet.getString("username"));
                loginDto.setStatus(sqlrowSet.getString("status"));
                loginDto.setAdded_by(sqlrowSet.getString("added_by"));

                // handle null safety for date
                if (sqlrowSet.getDate("added_on") != null) {
                    loginDto.setAdded_on(sqlrowSet.getDate("added_on").toLocalDate());
                }

                logger.info(" User fetched successfully with user_id: {}", loginDto.getUser_id());
            } else {
                logger.warn(" No user found for username: {}, roleCode: {}, status: {}", username, roleCode, status);
            }
        } catch (Exception e) {
            logger.error(" Error fetching user for username '{}' roleCode '{}' and status '{}': {}",
                    username, roleCode, status, e.getMessage());
        }

        return loginDto;
    }




    // Get last generated user_id from PMATM table, handle empty table
    public String getLastUserId() {
        String sql = "SELECT user_id FROM pmatm ORDER BY user_id DESC LIMIT 1";
        List<String> results = jdbcTemplate.queryForList(sql, String.class);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    @Override
    public List<Map<String,Object>> getActiveEmployeesAndInterns() {
        List<Map<String, Object>> user = new ArrayList<>();
        try {
            String sql = """
            SELECT p.user_id AS userId,
                   p.roleCode,
                   p.userType,
                   p.username,
                   p.status
            FROM user_management.pmatm p
            WHERE p.status = 'A'
              AND p.userType IN ('Employee', 'Intern')
              AND p.user_id NOT IN (
                  SELECT DISTINCT t.assign_to
                  FROM user_management.tasks t
                  WHERE t.status = 'A' 
                        AND t.assign_to IS NOT NULL
              )
            ORDER BY p.username ASC
            """;

            user = jdbcTemplate.queryForList(sql);

            logger.info("Retrieved {} active users (Employee/Intern) not currently assigned to tasks.", user.size());

        } catch (Exception e) {
            logger.error("Database error in getActiveEmployeesAndInterns: {}", e.getMessage(), e);
        }
        return user;
    }

    @Override
    public List<Map<String, Object>> getUnassignedInterns() {
        List<Map<String, Object>> result = new ArrayList<>();

        String sql = """
            SELECT p.user_id, p.username, p.roleCode, p.userType
            FROM user_management.pmatm p
            WHERE p.userType = 'Intern'
              AND p.status = 'A'
              AND p.user_id NOT IN (
                  SELECT DISTINCT assign_to FROM user_management.assign_tasks WHERE terminated_by IS NULL AND terminated_on IS NULL 
              )
            """;

        try {
            result = jdbcTemplate.queryForList(sql);
            logger.info("Fetched {} unassigned interns", result.size());
        } catch (Exception e) {
            logger.error("Error fetching unassigned interns: {}", e.getMessage(), e);
            throw e;
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getAllActiveInterns() {
        List<Map<String, Object>> result = new ArrayList<>();

        String sql = """
        SELECT p.user_id, p.username, p.roleCode, p.userType
        FROM user_management.pmatm p
        WHERE p.userType = 'Intern'
          AND p.status = 'A'
        """;

        try {
            result = jdbcTemplate.queryForList(sql);
            logger.info("Fetched {} active interns (assigned and unassigned)", result.size());
        } catch (Exception e) {
            logger.error("Error fetching active interns: {}", e.getMessage(), e);
            throw e;
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getActiveStaffWithTasks() {
        List<Map<String, Object>> result = new ArrayList<>();

        String sql = """
        SELECT p.user_id, p.username, p.roleCode, p.userType
        FROM user_management.pmatm p
        WHERE p.status = 'A'
          AND p.userType IN ('Intern', 'Employee')
          AND p.terminated_by IS NULL
          AND p.terminated_on IS NULL
        ORDER BY p.userType, p.username
    """;

        try {
            result = jdbcTemplate.queryForList(sql);
            logger.info("Fetched {} active employees and interns with no termination info", result.size());
        } catch (Exception e) {
            logger.error("Error fetching active staff with tasks: {}", e.getMessage(), e);
            throw e;
        }

        return result;
    }



    @Override
    public int countActiveUsers() {
        String sql = "SELECT COUNT(*) AS total_users " +
                "FROM pmatm " +
                "WHERE roleCode = 'PMATM' " +
                "AND terminated_on IS NULL " +
                "AND terminated_by IS NULL";

        try {
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
            if (sqlRowSet.next()) {
                return sqlRowSet.getInt("total_users");
            }
        } catch (Exception e) {
            logger.error("Cannot count users", e);
        }
        return 0; // fallback if query fails
    }


}
