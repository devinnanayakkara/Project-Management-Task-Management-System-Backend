package com.example.mobitel.Dao.Impl;

import com.example.mobitel.Controller.TaskController;
import com.example.mobitel.Dao.DashboardDao;
import com.example.mobitel.Dto.DashboardCountDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardDaoImpl implements DashboardDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(DashboardDaoImpl.class);

    @Override
    public DashboardCountDto getDashboardCounts(String username, String userType) {

        DashboardCountDto dto = new DashboardCountDto();

        try {

            /* ================= MANAGER ================= */
            if ("Manager".equalsIgnoreCase(userType)) {

                dto.setTotalUsers(jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM pmatm
                    WHERE roleCode = 'PMATM'
                      AND terminated_on IS NULL
                      AND terminated_by IS NULL
                """, Integer.class));

                dto.setAssignedTasks(jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM tasks
                    WHERE assign_to IS NOT NULL
                      AND status = 'A'
                      AND terminated_by IS NULL
                      AND terminated_on IS NULL
                """, Integer.class));

                dto.setActiveTasks(jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM tasks t
                    LEFT JOIN assign_tasks a ON t.task_id = a.task_id
                    WHERE t.status = 'A'
                      AND (a.status = 'A' OR a.status IS NULL)
                      AND t.terminated_by IS NULL
                      AND t.terminated_on IS NULL
                """, Integer.class));

                dto.setPendingProjects(jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM projects
                    WHERE status = 'P'
                      AND terminated_by IS NULL
                      AND terminated_on IS NULL
                """, Integer.class));

                dto.setCompletedTasks(jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM tasks t
                    JOIN assign_tasks a ON t.task_id = a.task_id
                    WHERE t.completed_by IS NOT NULL
                      AND t.completed_on IS NOT NULL
                      AND t.terminated_by IS NULL
                      AND t.terminated_on IS NULL
                """, Integer.class));
            }

            // EMPLOYEE
            if ("Employee".equalsIgnoreCase(userType)) {

                // Get the employee ID
                String userId = jdbcTemplate.queryForObject(
                        "SELECT user_id FROM pmatm WHERE username = ?",
                        String.class, username);

                // Employee total tasks (own tasks + all intern tasks)
                dto.setEmployeeTotalTasks(jdbcTemplate.queryForObject("""
                     SELECT COUNT(*)
                     FROM tasks t
                     LEFT JOIN pmatm u ON t.assign_to = u.user_id
                     WHERE t.status = 'A'
                     AND t.terminated_by IS NULL
                     AND t.terminated_on IS NULL
                     AND (t.assign_to = ? OR u.userType = 'Intern')
                     """, Integer.class, userId));

                // Employee assigned tasks (only own tasks)
                dto.setEmployeeAssignedTasks(jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM tasks t
                        LEFT JOIN pmatm u ON t.assign_to = u.user_id
                        WHERE u.username = ?
                        AND t.status = 'A'
                        AND t.terminated_by IS NULL
                        AND t.terminated_on IS NULL
                       """, Integer.class, username));

                // Intern total tasks (all interns, active tasks)
                dto.setInternTotalTasks(jdbcTemplate.queryForObject("""
                          SELECT COUNT(*)
                          FROM tasks t
                          LEFT JOIN pmatm u ON t.assign_to = u.user_id
                          WHERE u.userType = 'Intern'
                          AND t.status = 'A'
                          AND t.terminated_by IS NULL
                          AND t.terminated_on IS NULL
                          """, Integer.class));

                dto.setInternActiveTasks(dto.getInternTotalTasks());

                // Intern unassigned tasks (tasks with no assigned user)
                dto.setUnassignedTasks(jdbcTemplate.queryForObject("""
                   SELECT COUNT(*)
                   FROM tasks
                   WHERE assign_to IS NULL
                    AND status = 'p'
                    AND terminated_by IS NULL
                    AND terminated_on IS NULL
                """, Integer.class));
                 }


            // INTERN
            if ("Intern".equalsIgnoreCase(userType)) {

                dto.setInternTotalTasks(jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM tasks t
                    LEFT JOIN pmatm u ON t.assign_to = u.user_id
                    WHERE u.username = ?
                      AND t.status = 'A'
                      AND t.terminated_by IS NULL
                      AND t.terminated_on IS NULL
                """, Integer.class, username));

                dto.setInternActiveTasks(dto.getInternTotalTasks());
            }

        } catch (Exception e) {
            logger.error("Error fetching dashboard counts", e);
        }

        return dto;
    }
}

