package com.example.mobitel.Dao.Impl;

import com.example.mobitel.Dao.UserDao;

import com.example.mobitel.Dto.LoginDto;
import com.example.mobitel.Dto.ProjectDto;
import com.example.mobitel.Dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository // Marks this class as a DAO component for Spring
public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate; // Inject JdbcTemplate to execute SQL queries





    // Find User by Username
    @Override
    public LoginDto findUserByUsername(String username) {
        String sql = "SELECT * FROM user_management.pmatm WHERE username = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(LoginDto.class), username);
        } catch (EmptyResultDataAccessException e) {
            return null; // No result found
        }
    }




    @Override
    public int addUserToUserDetails(LoginDto loginDto) {
        try {
            //  Check if username already exists
            String checkSql = "SELECT COUNT(*) FROM user_management.user_details WHERE username = ?";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class, loginDto.getRequestUserId());

            if (count > 0) {
                throw new DuplicateKeyException("User with same username already exists.");
            }

            // Proceed with insert if not duplicate
            String sql = "INSERT INTO user_management.user_details " +
                    "(username, user_id, added_by, added_date, userType) " +
                    "VALUES (?, ?, ?, ?, ?)";

            return jdbcTemplate.update(sql,
                    loginDto.getRequestUserId(), // username
                    loginDto.getUser_id(),            // id
                    loginDto.getAdded_by(),      // added_by
                    Timestamp.valueOf(loginDto.getAdded_on().atStartOfDay()), // added_date
                    loginDto.getUserType()       // userType (Intern / Employee)
            );

        } catch (DuplicateKeyException e) {
            logger.warn("Duplicate entry detected: {}", e.getMessage());
            throw e; // Re-throw to service for rollback handling
        } catch (Exception e) {
            logger.error("Error adding user to user_details: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to insert user details due to internal error.", e);
        }
    }


    @Override
    public LoginDto findUserByUsernameOrEmail(String username, String email) {
        String sql = "SELECT * FROM user_management.user_details WHERE username = ? OR email = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(LoginDto.class), username, email);
        } catch (EmptyResultDataAccessException e) {
            return null; // no user found
        }
    }





}