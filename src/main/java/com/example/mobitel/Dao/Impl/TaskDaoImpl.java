package com.example.mobitel.Dao.Impl;

import com.example.mobitel.Config.Utility.JwtUtil;
import com.example.mobitel.Dao.TaskDao;
import com.example.mobitel.Dto.ProjectDto;
import com.example.mobitel.Dto.TaskDto;
import com.example.mobitel.Service.Impl.TaskServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class TaskDaoImpl implements TaskDao {

    private static final Logger logger = LoggerFactory.getLogger(TaskDaoImpl.class);

    @Autowired
    private  JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public int addTask(TaskDto request) {
        try {
            String sql = "INSERT INTO tasks" + "(task_id,task_name,task_description,start_date,expected_end_date,actual_end_date,added_by,added_on,project_id,status)" + "VALUES(?,?,?,?,?,?,?,?,?,?)";

            return jdbcTemplate.update(sql,
                    request.getTask_id(),
                    request.getTask_name(),
                    request.getTask_description(),
                    request.getStart_date(),
                    request.getExpected_end_date(),
                    request.getActual_end_date(),
                    request.getAdded_by(),
                    request.getAdded_on(),
                    request.getProject_id(),
                    request.getStatus()

                    );

            //String UpdateProjectSql = "UPDATE "


        } catch (Exception e) {
            logger.error("Error saving a task: {}", e.getMessage());
            return 0;
        }
    }



    @Override
    public TaskDto findTaskByName(String taskName) {
        TaskDto taskDto = null;

        String sql = "SELECT * FROM tasks " +
                "WHERE task_name = ? " +
                "AND (status = 'A' OR status IS NULL) " +
                "AND (terminated_by IS NULL OR terminated_on IS NULL)";

        try {
            SqlRowSet rowset = jdbcTemplate.queryForRowSet(sql, taskName);
            if (rowset.next()) {
                taskDto = new TaskDto();
                taskDto.setTask_id(rowset.getString("task_id"));
                taskDto.setTask_name(rowset.getString("task_name"));
                taskDto.setTask_description(rowset.getString("task_description"));
                taskDto.setStart_date(rowset.getDate("start_date").toLocalDate());
                taskDto.setExpected_end_date(rowset.getDate("expected_end_date").toLocalDate());
                taskDto.setActual_end_date(rowset.getDate("actual_end_date").toLocalDate());
                taskDto.setAdded_by(rowset.getString("added_by"));
                taskDto.setAdded_on(rowset.getDate("added_on").toLocalDate());

                taskDto.setProject_id(rowset.getInt("project_id"));
                taskDto.setStatus(rowset.getString("status"));

            }
        } catch (Exception e) {
            logger.error("Error finding task by name '{}': {}", taskName, e.getMessage(), e);
        }

        return taskDto;
    }



    @Override
    public TaskDto findTaskByDescription(String taskDescription) {
        TaskDto taskDto = null;

        String sql = "SELECT * FROM tasks " +
                "WHERE task_description = ? " +
                "AND (status = 'A' OR status IS NULL) " +
                "AND (terminated_by IS NULL OR terminated_on IS NULL)";

        try {
            SqlRowSet rowset = jdbcTemplate.queryForRowSet(sql, taskDescription);
            if (rowset.next()) {
                taskDto = new TaskDto();
                taskDto.setTask_id(rowset.getString("task_id"));
                taskDto.setTask_name(rowset.getString("task_name"));
                taskDto.setTask_description(rowset.getString("task_description"));

                if (rowset.getDate("start_date") != null)
                    taskDto.setStart_date(rowset.getDate("start_date").toLocalDate());
                if (rowset.getDate("expected_end_date") != null)
                    taskDto.setExpected_end_date(rowset.getDate("expected_end_date").toLocalDate());
                if (rowset.getDate("actual_end_date") != null)
                    taskDto.setActual_end_date(rowset.getDate("actual_end_date").toLocalDate());
                if (rowset.getDate("added_on") != null)
                    taskDto.setAdded_on(rowset.getDate("added_on").toLocalDate());

                taskDto.setAdded_by(rowset.getString("added_by"));
                taskDto.setProject_id(rowset.getInt("project_id"));
                taskDto.setStatus(rowset.getString("status"));
            }
        } catch (Exception e) {
            logger.error("Error finding task by name '{}': {}", taskDescription, e.getMessage(), e);
        }

        return taskDto;
    }

    @Override
    public List<TaskDto> viewAllTasks() {

        String sql = "SELECT t.*, " +
                "p.project_name, " +
                "u.username AS assign_to_name, " +
                "s.status_name " +
                "FROM tasks t " +
                "LEFT JOIN projects p ON t.project_id = p.project_id " +
                "LEFT JOIN pmatm u ON t.assign_to = u.user_id " +
                "LEFT JOIN status s ON t.status = s.status_id " +
                "WHERE (t.status IN ('A','C','D','P')) " +
                "AND t.terminated_by IS NULL " +
                "AND t.terminated_on IS NULL";

        List<TaskDto> tasks = new ArrayList<>();

        try {
            SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);

            while (rs.next()) {
                TaskDto dto = new TaskDto();

                dto.setTask_id(rs.getString("task_id"));
                dto.setTask_name(rs.getString("task_name"));
                dto.setTask_description(rs.getString("task_description"));

                dto.setStart_date(rs.getDate("start_date").toLocalDate());
                dto.setExpected_end_date(rs.getDate("expected_end_date").toLocalDate());

                java.sql.Date actual = rs.getDate("actual_end_date");
                dto.setActual_end_date(actual != null ? actual.toLocalDate() : null);

                dto.setAdded_on(rs.getDate("added_on").toLocalDate());
                dto.setAdded_by(rs.getString("added_by"));

                dto.setProject_id(rs.getInt("project_id"));
                dto.setProject_name(rs.getString("project_name"));

                dto.setAssign_to(rs.getString("assign_to_name"));

                dto.setStatus_id(rs.getString("status"));
                dto.setStatus_name(rs.getString("status_name"));

                tasks.add(dto);
            }

        } catch (Exception e) {
            logger.error("Cannot fetch all tasks", e);
        }

        return tasks;
    }





    @Override
    public TaskDto findTaskByNameAndStatus(String taskName, String status) {
        String sql = "SELECT * FROM tasks " +
                "WHERE task_name = ? " +
                "AND status = ? " +
                "AND (terminated_by IS NULL OR terminated_on IS NULL)";

        TaskDto taskDto = null;

        try {
            SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, taskName, status);

            if (rowSet.next()) {
                taskDto = new TaskDto();
                taskDto.setTask_id(rowSet.getString("task_id"));
                taskDto.setTask_name(rowSet.getString("task_name"));
                taskDto.setTask_description(rowSet.getString("task_description"));
                taskDto.setStart_date(rowSet.getDate("start_date").toLocalDate());
                taskDto.setExpected_end_date(rowSet.getDate("expected_end_date").toLocalDate());
                if (rowSet.getDate("actual_end_date") != null)
                    taskDto.setActual_end_date(rowSet.getDate("actual_end_date").toLocalDate());
                taskDto.setAdded_by(rowSet.getString("added_by"));
                if (rowSet.getDate("added_on") != null)
                    taskDto.setAdded_on(rowSet.getDate("added_on").toLocalDate());
                taskDto.setProject_id(rowSet.getInt("project_id"));
                taskDto.setStatus(rowSet.getString("status"));

            }

        } catch (Exception e) {
            logger.error("Cannot find task with name '{}' and status '{}'", taskName, status, e);
        }

        return taskDto;
    }





    @Override
    public int updateTask(TaskDto request) {
        try {
            //  Step 1: Fetch the old record
            String fetchSql = "SELECT * FROM tasks WHERE task_id = ?";
            TaskDto oldTask = jdbcTemplate.queryForObject(fetchSql, new Object[]{request.getTask_id()}, (rs, rowNum) -> {
                TaskDto task = new TaskDto();
                task.setTask_id(rs.getString("task_id"));
                task.setTask_name(rs.getString("task_name"));
                task.setTask_description(rs.getString("task_description"));
                task.setComment(rs.getString("comment"));

                if (rs.getDate("start_date") != null)
                    task.setStart_date(rs.getDate("start_date").toLocalDate());
                if (rs.getDate("expected_end_date") != null)
                    task.setExpected_end_date(rs.getDate("expected_end_date").toLocalDate());
                if (rs.getDate("actual_end_date") != null)
                    task.setActual_end_date(rs.getDate("actual_end_date").toLocalDate());
                if (rs.getDate("added_on") != null)
                    task.setAdded_on(rs.getDate("added_on").toLocalDate());

                task.setAdded_by(rs.getString("added_by"));
                task.setTerminated_by(rs.getString("terminated_by"));
                task.setTerminated_on(rs.getString("terminated_on"));
                task.setProject_id(rs.getInt("project_id"));
                task.setStatus(rs.getString("status"));
                task.setAssign_to(rs.getString("assign_to"));
                return task;
            });

            if (oldTask == null) {
                throw new RuntimeException("Old task not found for ID: " + request.getTask_id());
            }

            //  Step 2: Terminate old task and assign_task records
            jdbcTemplate.update("UPDATE tasks SET terminated_by = ?, terminated_on = NOW() WHERE task_id = ?",
                    request.getAdded_by(), request.getTask_id());
            jdbcTemplate.update("UPDATE assign_tasks SET terminated_by = ?, terminated_on = NOW() WHERE task_id = ?",
                    request.getAdded_by(), request.getTask_id());

            //  Step 3: Insert new task
            String insertTaskSql = """
            INSERT INTO tasks 
            (task_name, task_description, comment, start_date, expected_end_date, added_by, added_on, status, project_id, assign_to)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

            jdbcTemplate.update(insertTaskSql,
                    (request.getTask_name() != null && !request.getTask_name().isEmpty()) ? request.getTask_name() : oldTask.getTask_name(),
                    (request.getTask_description() != null && !request.getTask_description().isEmpty()) ? request.getTask_description() : oldTask.getTask_description(),
                    (request.getComment() != null && !request.getComment().isEmpty()) ? request.getComment() : oldTask.getComment(),
                    (request.getStart_date() != null) ? request.getStart_date() : oldTask.getStart_date(),
                    (request.getExpected_end_date() != null) ? request.getExpected_end_date() : oldTask.getExpected_end_date(),
                    request.getAdded_by(),
                    request.getAdded_on(),
                    oldTask.getStatus(),
                    (request.getProject_id() != null) ? request.getProject_id() : oldTask.getProject_id(),
                    (request.getAssign_to() != null) ? request.getAssign_to() : oldTask.getAssign_to()
            );

            //  Step 4: Get new task_id
            Long newTaskId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

            //  Step 5: Insert into assign_tasks with the new task_id
            String insertAssignSql = """
            INSERT INTO assign_tasks 
            (task_id, project_id, assign_to, added_by, added_on, status, start_date, expected_end_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

            jdbcTemplate.update(insertAssignSql,
                    newTaskId,
                    (request.getProject_id() != null) ? request.getProject_id() : oldTask.getProject_id(),
                    (request.getAssign_to() != null) ? request.getAssign_to() : oldTask.getAssign_to(),
                    request.getAdded_by(),
                    request.getAdded_on(),
                    oldTask.getStatus(),
                    (request.getStart_date() != null) ? request.getStart_date() : oldTask.getStart_date(),
                    (request.getExpected_end_date() != null) ? request.getExpected_end_date() : oldTask.getExpected_end_date()
            );

            return 1;

        } catch (Exception e) {
            logger.error(" Error updating (inserting new) task and assign_tasks: {}", e.getMessage(), e);
            return 0;
        }
    }






//    @Override
//    public TaskDto findAssignTask(String task_id, String assign_to) {
//        String sql = "SELECT * FROM assign_tasks WHERE task_id = ? AND assign_to = ? " +
//                "AND (terminated_by IS NULL OR terminated_on IS NULL)";
//
//        try {
//            SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, task_id, assign_to);
//            if (rs.next()) {
//                TaskDto dto = new TaskDto();
//                dto.setTask_id(rs.getString("task_id"));
//                dto.setProject_id(rs.getInt("project_id"));
//                dto.setAssign_to(rs.getString("assign_to"));
//                dto.setAdded_by(rs.getString("added_by"));
//                dto.setAdded_on(rs.getDate("added_on").toLocalDate());
//                dto.setStatus(rs.getString("status"));
//                dto.setStart_date(rs.getDate("start_date").toLocalDate());
//                dto.setExpected_end_date(rs.getDate("expected_end_date").toLocalDate());
//                if (rs.getDate("actual_end_date") != null)
//                    dto.setActual_end_date(rs.getDate("actual_end_date").toLocalDate());
//                return dto;
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Error finding assigned task: " + e.getMessage(), e);
//        }
//        return null;
//    }


//    @Transactional
//    @Override
//    public int assignTask(TaskDto request) {
//        try {
//            String insertSql = "INSERT INTO assign_tasks " +
//                    "(task_id, project_id, assign_to, added_by, added_on, status, start_date, expected_end_date, actual_end_date) " +
//                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//
//            int rows = jdbcTemplate.update(insertSql,
//                    request.getTask_id(),
//                    request.getProject_id(),
//                    request.getAssign_to(),
//                    request.getAdded_by(),
//                    request.getAdded_on(),
//                    request.getStatus(),
//                    request.getStart_date(),
//                    request.getExpected_end_date(),
//                    request.getActual_end_date());
//
//            // update tasks table
//            jdbcTemplate.update("UPDATE tasks SET assign_to = ? ,status='A' WHERE task_id = ?",
//                    request.getAssign_to(), request.getTask_id());
//
//            return rows;
//        } catch (Exception e) {
//            throw new RuntimeException("Error while assigning task: " + e.getMessage(), e);
//        }
//    }

    @Override
    public int assignTask(TaskDto request) {
        // Terminate previous active task
        String terminateSql = "UPDATE tasks SET terminated_by = ?, terminated_on = NOW() WHERE task_id = ?";
        jdbcTemplate.update(terminateSql, request.getAdded_by(), request.getTask_id());

        // Fetch task_name and task_description from the last task record
        TaskDto lastTask = jdbcTemplate.queryForObject(
                "SELECT task_name, task_description FROM tasks WHERE task_id=? ORDER BY added_on DESC LIMIT 1",
                new Object[]{request.getTask_id()},
                (rs, rowNum) -> {
                    TaskDto t = new TaskDto();
                    t.setTask_name(rs.getString("task_name"));
                    t.setTask_description(rs.getString("task_description"));
                    return t;
                }
        );

        // Insert into assign_tasks (keep task_id as reference to original task)
        String insertAssignSql = "INSERT INTO assign_tasks " +
                "(task_id, project_id, assign_to, added_by, added_on, status, start_date, expected_end_date, actual_end_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int rows = jdbcTemplate.update(insertAssignSql,
                request.getTask_id(),
                request.getProject_id(),
                request.getAssign_to(),
                request.getAdded_by(),
                LocalDate.now(),
                "A",
                request.getStart_date(),
                request.getExpected_end_date(),
                request.getActual_end_date()
        );

        // Insert new task row (historical record) WITHOUT task_id (auto-increment)
        String insertTaskSql = "INSERT INTO tasks " +
                "(task_name, task_description, start_date, expected_end_date, actual_end_date, added_by, added_on, project_id, status, assign_to) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(insertTaskSql,
                lastTask.getTask_name(),
                lastTask.getTask_description(),
                request.getStart_date(),
                request.getExpected_end_date(),
                request.getActual_end_date(),
                request.getAdded_by(),
                LocalDate.now(),
                request.getProject_id(),
                "A",              // active
                request.getAssign_to()  // assigned user
        );

        return rows;
    }







    @Override
    public List<TaskDto> getUnassignedTasksByProject(Integer project_id) {
        List<TaskDto> taskList = new ArrayList<>();

        try {
            String sql = """
            SELECT t.task_id,
                   t.task_name,
                   t.task_description,
                   t.start_date,
                   t.expected_end_date,
                   t.actual_end_date,
                   t.added_on,
                   t.added_by,
                   t.project_id,
                   p.project_name,
                   t.status
            FROM user_management.tasks t
            JOIN user_management.projects p ON t.project_id = p.project_id
            WHERE t.project_id = ?
              AND t.status = 'A'
              AND (t.assign_to IS NULL OR t.assign_to = '')
            ORDER BY t.task_name ASC
            """;

            taskList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TaskDto.class), project_id);
            logger.info("Retrieved {} unassigned tasks for project ID {}", taskList.size(), project_id);

        } catch (Exception e) {
            logger.error("Database error in getUnassignedTasksByProject: {}", e.getMessage(), e);
        }

        return taskList;
    }





    @Override
    public List<TaskDto> getAllAssignedAndOngoingTasks() {
        try {
            String sql = "SELECT t.task_id, t.task_name, t.task_description, t.start_date, " +
                    "t.expected_end_date, t.actual_end_date, t.project_id," +
                    "t.assign_to, u.username AS assign_to_username, t.status " +
                    "FROM tasks t " +
                    "LEFT JOIN pmatm u ON t.assign_to = u.user_id " +
                    "WHERE t.assign_to IS NOT NULL " +
                    "  AND t.status = 'A' " +
                    " AND t.terminated_by IS NULL " +
                    "  AND t.terminated_on IS NULL";

            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TaskDto.class));
        } catch (Exception e) {
            logger.error("Error fetching assigned and ongoing tasks: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

//    @Override
//    public Map<String, List<TaskDto>> getTasksOverviewForUser(String username) {
//        Map<String, List<TaskDto>> result = new HashMap<>();
//
//        try {
//            // 1. Tasks assigned to logged-in user
//            String sqlUserTasks = """
//            SELECT t.task_id, t.task_name, t.task_description, t.start_date,
//                   t.expected_end_date, t.actual_end_date, t.project_id,
//                   t.assign_to, u.username AS assign_to_username, t.status
//            FROM tasks t
//            LEFT JOIN pmatm u ON t.assign_to = u.user_id
//            WHERE u.username = ?
//              AND t.status = 'A'
//              AND t.terminated_by IS NULL
//              AND t.terminated_on IS NULL
//        """;
//            List<TaskDto> userTasks = jdbcTemplate.query(sqlUserTasks, new Object[]{username}, new BeanPropertyRowMapper<>(TaskDto.class));
//            result.put("userTasks", userTasks);
//
//
//            String sqlUnassignedTasks = """
//            SELECT t.task_id, t.task_name, t.task_description, t.start_date,
//                   t.expected_end_date, t.actual_end_date, t.project_id,
//                   t.assign_to, u.username AS assign_to_username, t.status
//            FROM tasks t
//            LEFT JOIN pmatm u ON t.assign_to = u.user_id
//            WHERE t.assign_to IS NULL
//              AND t.status = 'P'
//              AND t.terminated_by IS NULL
//              AND t.terminated_on IS NULL
//        """;
//            List<TaskDto> unassignedTasks = jdbcTemplate.query(sqlUnassignedTasks, new BeanPropertyRowMapper<>(TaskDto.class));
//            result.put("unassignedTasks", unassignedTasks);
//
//            // 3. Tasks assigned to interns
//            String sqlInternTasks = """
//            SELECT t.task_id, t.task_name, t.task_description, t.start_date,
//                   t.expected_end_date, t.actual_end_date, t.project_id,
//                   t.assign_to, u.username AS assign_to_username, t.status
//            FROM tasks t
//            LEFT JOIN pmatm u ON t.assign_to = u.user_id
//            WHERE u.userType = 'Intern'
//              AND t.status = 'A'
//              AND t.terminated_by IS NULL
//              AND t.terminated_on IS NULL
//        """;
//            List<TaskDto> internTasks = jdbcTemplate.query(sqlInternTasks, new BeanPropertyRowMapper<>(TaskDto.class));
//            result.put("internTasks", internTasks);
//
//        } catch (Exception e) {
//            logger.error("Error fetching tasks overview: {}", e.getMessage(), e);
//        }
//
//        return result;
//    }

    @Override
    public Map<String, List<TaskDto>> getTasksOverviewForUser(String username) {
        Map<String, List<TaskDto>> result = new HashMap<>();

        try {

            //  Tasks assigned to logged-in user
            String sqlUserTasks = """
            SELECT t.task_id, t.task_name, t.task_description, t.start_date,
                   t.expected_end_date, t.actual_end_date, t.project_id,
                   t.assign_to, u.username AS assign_to_username,
                   t.status AS status_id, s.status_name
            FROM tasks t
            LEFT JOIN pmatm u ON t.assign_to = u.user_id
            LEFT JOIN status s ON t.status = s.status_id
            WHERE u.username = ?
              AND t.status = 'A'
              AND t.terminated_by IS NULL
              AND t.terminated_on IS NULL
        """;

            List<TaskDto> userTasks = jdbcTemplate.query(
                    sqlUserTasks,
                    new Object[]{username},
                    new BeanPropertyRowMapper<>(TaskDto.class)
            );
            result.put("userTasks", userTasks);


            //  Unassigned Tasks
            String sqlUnassignedTasks = """
            SELECT t.task_id, t.task_name, t.task_description, t.start_date,
                   t.expected_end_date, t.actual_end_date, t.project_id,
                   t.assign_to, u.username AS assign_to_username,
                   t.status AS status_id, s.status_name
            FROM tasks t
            LEFT JOIN pmatm u ON t.assign_to = u.user_id
            LEFT JOIN status s ON t.status = s.status_id
            WHERE t.assign_to IS NULL
              AND t.status = 'P'
              AND t.terminated_by IS NULL
              AND t.terminated_on IS NULL
        """;

            List<TaskDto> unassignedTasks = jdbcTemplate.query(
                    sqlUnassignedTasks,
                    new BeanPropertyRowMapper<>(TaskDto.class)
            );
            result.put("unassignedTasks", unassignedTasks);


            // Tasks assigned to interns
            String sqlInternTasks = """
            SELECT t.task_id, t.task_name, t.task_description, t.start_date,
                   t.expected_end_date, t.actual_end_date, t.project_id,
                   t.assign_to, u.username AS assign_to_username,
                   t.status AS status_id, s.status_name
            FROM tasks t
            LEFT JOIN pmatm u ON t.assign_to = u.user_id
            LEFT JOIN status s ON t.status = s.status_id
            WHERE u.userType = 'Intern'
              AND t.status = 'A'
              AND t.terminated_by IS NULL
              AND t.terminated_on IS NULL
        """;

            List<TaskDto> internTasks = jdbcTemplate.query(
                    sqlInternTasks,
                    new BeanPropertyRowMapper<>(TaskDto.class)
            );
            result.put("internTasks", internTasks);

        } catch (Exception e) {
            logger.error("Error fetching tasks overview: {}", e.getMessage(), e);
        }

        return result;
    }


    @Override
    @Transactional
    public int completeTask(TaskDto request) {
        String updateTasksSql = "UPDATE tasks " +
                "SET completed_by = ?, " +
                "completed_on = ?, " +
                "status = 'C', " +
                "actual_end_date = ? " +
                "WHERE task_id = ? " +
                "AND status = 'A' " +
                "AND terminated_by IS NULL " +
                "AND terminated_on IS NULL";

        String updateAssignTaskSql = "UPDATE assign_tasks " +
                "SET status = 'C', " +
                "actual_end_date = ? " +
                "WHERE task_id = ? " +
                "AND status = 'A'";

        try {
            LocalDateTime completedOn = LocalDateTime.now();

            // Convert LocalDate (from frontend/DTO) → Timestamp safely
            Timestamp actualEndDate = null;
            if (request.getActual_end_date() != null) {
                LocalDate localDate = request.getActual_end_date();
                actualEndDate = Timestamp.valueOf(localDate.atStartOfDay());
            }

            //  Update `tasks` table
            int rowsUpdated = jdbcTemplate.update(updateTasksSql,
                    request.getCompleted_by(),
                    Timestamp.valueOf(completedOn),
                    actualEndDate,
                    request.getTask_id()
            );

            //  Only update `assign_task` if `tasks` update succeeded
            if (rowsUpdated > 0) {
                jdbcTemplate.update(updateAssignTaskSql,
                        actualEndDate,
                        request.getTask_id()
                );
            }

            return rowsUpdated;

        } catch (DataAccessException e) {
            logger.error("Error completing task_id {}: {}", request.getTask_id(), e.getMessage());
            throw new RuntimeException("Failed to complete task and update assign_task table", e);
        }
    }




    @Override
    public Map<String, List<TaskDto>> getUserAndInternTasks(String username) {
        Map<String, List<TaskDto>> result = new HashMap<>();

        try {
            //  Tasks assigned to the logged-in user
            String sqlUserTasks = """
            SELECT t.task_id, t.task_name, t.task_description, t.start_date,
                   t.expected_end_date, t.actual_end_date, t.project_id,
                   t.assign_to, u.username AS assign_to_username, t.status
            FROM user_management.tasks t
            LEFT JOIN user_management.pmatm u ON t.assign_to = u.user_id
            WHERE u.username = ?
              AND t.status = 'A'
              AND t.terminated_by IS NULL
              AND t.terminated_on IS NULL
        """;
            List<TaskDto> userTasks = jdbcTemplate.query(
                    sqlUserTasks, new Object[]{username}, new BeanPropertyRowMapper<>(TaskDto.class));
            result.put("userTasks", userTasks);

            //  Tasks assigned to interns
            String sqlInternTasks = """
            SELECT t.task_id, t.task_name, t.task_description, t.start_date,
                   t.expected_end_date, t.actual_end_date, t.project_id,
                   t.assign_to, u.username AS assign_to_username, t.status
            FROM user_management.tasks t
            LEFT JOIN user_management.pmatm u ON t.assign_to = u.user_id
            WHERE u.userType = 'Intern'
              AND t.status = 'A'
              AND t.terminated_by IS NULL
              AND t.terminated_on IS NULL
        """;
            List<TaskDto> internTasks = jdbcTemplate.query(
                    sqlInternTasks, new BeanPropertyRowMapper<>(TaskDto.class));
            result.put("internTasks", internTasks);

            logger.info("Fetched {} user tasks and {} intern tasks",
                    userTasks.size(), internTasks.size());

        } catch (Exception e) {
            logger.error("Error fetching user and intern tasks: {}", e.getMessage(), e);
        }

        return result;
    }




    @Override
    public List<TaskDto> getLoggedUserTasks(String username) {
        List<TaskDto> result = new ArrayList<>();

        try {
            String sql = """
            SELECT t.task_id, t.task_name, t.task_description, t.start_date,
                   t.expected_end_date, t.actual_end_date, t.project_id,
                   t.assign_to, u.username AS assign_to_username, t.status
            FROM user_management.tasks t
            LEFT JOIN user_management.pmatm u ON t.assign_to = u.user_id
            WHERE u.username = ?
              AND t.status = 'A'
              AND t.terminated_by IS NULL
              AND t.terminated_on IS NULL
        """;

            result = jdbcTemplate.query(
                    sql,
                    new Object[]{username},
                    new BeanPropertyRowMapper<>(TaskDto.class)
            );

            logger.info("Fetched {} tasks for logged user {}", result.size(), username);

        } catch (Exception e) {
            logger.error("Error fetching logged user tasks: {}", e.getMessage(), e);
        }

        return result;
    }




    @Override
    @Transactional
    public int terminateAndInsertTask(String oldTaskId, TaskDto newTaskRequest) {
        try {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            // Terminate existing ACTIVE task row
            String terminateTaskSql =
                    "UPDATE tasks SET status='D', terminated_by=?, terminated_on=? WHERE task_id=? AND status='A'";

            // Terminate existing ACTIVE assignment row (if exists)
            String terminateAssignSql =
                    "UPDATE assign_tasks SET status='D', terminated_by=?, terminated_on=? WHERE task_id=? AND status='A'";

            int taskTerminated = jdbcTemplate.update(terminateTaskSql,
                    newTaskRequest.getAdded_by(), now, oldTaskId);

            int assignTerminated = jdbcTemplate.update(terminateAssignSql,
                    newTaskRequest.getAdded_by(), now, oldTaskId);

            // Task MUST be active
            if (taskTerminated <= 0) {
                throw new RuntimeException("No active task found to terminate — rolling back.");
            }

            // Assignment is OPTIONAL
            if (assignTerminated == 0) {
                logger.warn("No active assignment found for task {} — continuing swap.", oldTaskId);
            }

            // Fetch values from last task
            Map<String, Object> oldTask = jdbcTemplate.queryForMap(
                    "SELECT task_name, task_description, project_id FROM tasks WHERE task_id = ?",
                    oldTaskId
            );

            String taskName = newTaskRequest.getTask_name() != null && !newTaskRequest.getTask_name().isEmpty()
                    ? newTaskRequest.getTask_name()
                    : (String) oldTask.get("task_name");

            String taskDescription = newTaskRequest.getTask_description() != null && !newTaskRequest.getTask_description().isEmpty()
                    ? newTaskRequest.getTask_description()
                    : (String) oldTask.get("task_description");

            Integer projectId = newTaskRequest.getProject_id() != null
                    ? newTaskRequest.getProject_id()
                    : (Integer) oldTask.get("project_id");

            // Insert new task
            String insertTaskSql =
                    "INSERT INTO tasks (task_name, task_description, project_id, added_by, added_on, status, start_date, expected_end_date, assign_to) " +
                            "VALUES (?, ?, ?, ?, ?, 'A', ?, ?, ?)";

            jdbcTemplate.update(insertTaskSql,
                    taskName,
                    taskDescription,
                    projectId,
                    newTaskRequest.getAdded_by(),
                    now,
                    newTaskRequest.getStart_date(),
                    newTaskRequest.getExpected_end_date(),
                    newTaskRequest.getAssign_to()
            );

            Integer newTaskId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

            if (newTaskId == null) {
                throw new RuntimeException("Failed to get new task_id — rolling back.");
            }

            // Insert new assignment
            String insertAssignSql =
                    "INSERT INTO assign_tasks (task_id, project_id, assign_to, added_by, added_on, status, start_date, expected_end_date) " +
                            "VALUES (?, ?, ?, ?, ?, 'A', ?, ?)";

            jdbcTemplate.update(insertAssignSql,
                    newTaskId,
                    projectId,
                    newTaskRequest.getAssign_to(),
                    newTaskRequest.getAdded_by(),
                    now,
                    newTaskRequest.getStart_date(),
                    newTaskRequest.getExpected_end_date()
            );

            return 1;

        } catch (Exception e) {
            logger.error("Unexpected error during swap for {}: {}", oldTaskId, e.getMessage(), e);
            throw new RuntimeException("Unexpected error during swap — rolled back.", e);
        }
    }

    @Override
    public List<TaskDto> getActiveTasks() {
        try {
            String sql = "SELECT " +
                    "t.task_id, " + "t.task_name, " + "t.task_description, " + "t.start_date, " + "t.expected_end_date, " + "t.actual_end_date, " + "t.project_id, " +
                    "a.assign_to, " +
                    "u.username AS assign_to_username, " +
                    "t.status " +
                    "FROM tasks t " +
                    "LEFT JOIN assign_tasks a ON t.task_id = a.task_id " +
                    "LEFT JOIN user_details u ON a.assign_to = u.user_id " +
                    "WHERE t.status = 'A' " +
                    "  AND (a.status = 'A' OR a.status IS NULL) " +
                    "  AND t.terminated_by IS NULL " +
                    "  AND t.terminated_on IS NULL";

            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TaskDto.class));
        } catch (Exception e) {
            logger.error("Error fetching active tasks (assigned + unassigned): {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<TaskDto> getCompletedTasks() {
        try {
            String sql = """
            SELECT t.task_id, t.task_name, t.task_description, t.start_date, 
                   t.expected_end_date, t.actual_end_date, t.project_id,
                   a.assign_to, u.username AS assign_to_username, t.status
            FROM tasks t
            JOIN assign_tasks a ON t.task_id = a.task_id
            LEFT JOIN user_details u ON a.assign_to = u.user_id
            WHERE t.completed_by IS NOT NULL
              AND t.completed_on IS NOT NULL
        """;

            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TaskDto.class));
        } catch (Exception e) {
            logger.error("Error fetching completed tasks: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    @Override
    public List<TaskDto> getActiveTasksAssignedToInterns() {
        try {
            String sql = "SELECT " +
                    "t.task_id, " +
                    "t.task_name, " +
                    "t.task_description, " +
                    "t.start_date, " +
                    "t.expected_end_date, " +
                    "t.actual_end_date, " +
                    "t.project_id, " +
                    "a.assign_to, " +
                    "u.username AS assign_to_username, " +
                    "t.status " +
                    "FROM tasks t " +
                    "INNER JOIN assign_tasks a ON t.task_id = a.task_id " +
                    "INNER JOIN user_details u ON a.assign_to = u.user_id " +
                    "INNER JOIN pmatm p ON u.user_id = p.user_id " +   //  Join with pmatm
                    "WHERE t.status = 'A' " +
                    "  AND a.status = 'A' " +
                    "  AND p.userType = 'Intern' " +                   //  Filter interns
                    "  AND t.terminated_by IS NULL " +
                    "  AND t.terminated_on IS NULL";

            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TaskDto.class));
        } catch (Exception e) {
            logger.error("Error fetching active tasks assigned to interns: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }



    @Override
    public int countCompletedTasks() {
        String sql = """
        SELECT COUNT(*) AS total_completed
        FROM tasks t
        JOIN assign_tasks a ON t.task_id = a.task_id
        WHERE t.completed_by IS NOT NULL
          AND t.completed_on IS NOT NULL
          AND t.terminated_by IS NULL
          AND t.terminated_on IS NULL
    """;

        try {
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
            if (sqlRowSet.next()) {
                return sqlRowSet.getInt("total_completed");
            }
        } catch (Exception e) {
            logger.error("Error counting completed tasks", e);
        }
        return 0;
    }


    @Override
    public List<TaskDto> getCompletedTasksAssignedToInterns() {
        try {
            String sql = """
            SELECT 
                t.task_id, 
                t.task_name, 
                t.task_description, 
                t.start_date, 
                t.expected_end_date, 
                t.actual_end_date, 
                t.project_id,
                a.assign_to, 
                u.username AS assign_to_username, 
                t.status
            FROM tasks t
            INNER JOIN assign_tasks a ON t.task_id = a.task_id
            INNER JOIN user_details u ON a.assign_to = u.user_id
            INNER JOIN pmatm p ON u.user_id = p.user_id
            WHERE t.completed_by IS NOT NULL
              AND t.completed_on IS NOT NULL
              AND p.userType = 'Intern'
              AND a.status = 'C'
              AND t.status = 'C'
        """;

            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TaskDto.class));
        } catch (Exception e) {
            logger.error("Error fetching completed tasks assigned to interns: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    @Override
    public int countAssignedAndOngoingTasks() {
        String sql = "SELECT COUNT(*) AS total_tasks " +
                "FROM tasks " +
                "WHERE assign_to IS NOT NULL " +
                "AND status = 'A' " +
                "AND terminated_by IS NULL " +
                "AND terminated_on IS NULL";

        try {
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
            if (sqlRowSet.next()) {
                return sqlRowSet.getInt("total_tasks");
            }
        } catch (Exception e) {
            logger.error("Error counting assigned and ongoing tasks", e);
        }
        return 0;
    }


    @Override
    public int countActiveTasks() {
        String sql = "SELECT COUNT(*) AS total_tasks " +
                "FROM tasks t " +
                "LEFT JOIN assign_tasks a ON t.task_id = a.task_id " +
                "WHERE t.status = 'A' " +
                "  AND (a.status = 'A' OR a.status IS NULL) " +
                "  AND t.terminated_by IS NULL " +
                "  AND t.terminated_on IS NULL";

        try {
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
            if (sqlRowSet.next()) {
                return sqlRowSet.getInt("total_tasks");
            }
        } catch (Exception e) {
            logger.error("Error counting active tasks (assigned + unassigned)", e);
        }
        return 0;
    }

    @Override
    public Map<String, Integer> getUserInternAndUnassignedTaskCounts(String username) {
        Map<String, Integer> result = new HashMap<>();

        try {
            // Count tasks assigned to logged user
            String sqlUserCount = """
            SELECT COUNT(*) 
            FROM user_management.tasks t
            LEFT JOIN user_management.pmatm u ON t.assign_to = u.user_id
            WHERE u.username = ?
              AND t.status = 'A'
              AND t.terminated_by IS NULL
              AND t.terminated_on IS NULL
        """;
            Integer userTaskCount = jdbcTemplate.queryForObject(sqlUserCount, Integer.class, username);
            result.put("userTaskCount", userTaskCount);

            // Count tasks assigned to interns
            String sqlInternCount = """
            SELECT COUNT(*)
            FROM user_management.tasks t
            LEFT JOIN user_management.pmatm u ON t.assign_to = u.user_id
            WHERE u.userType = 'Intern'
              AND t.status = 'A'
              AND t.terminated_by IS NULL
              AND t.terminated_on IS NULL
        """;
            Integer internTaskCount = jdbcTemplate.queryForObject(sqlInternCount, Integer.class);
            result.put("internTaskCount", internTaskCount);

            // Count unassigned tasks
            String sqlUnassignedCount = """
            SELECT COUNT(*)
            FROM user_management.tasks t
            WHERE t.assign_to IS NULL
              AND t.status = 'A'
              AND t.terminated_by IS NULL
              AND t.terminated_on IS NULL
        """;
            Integer unassignedTaskCount = jdbcTemplate.queryForObject(sqlUnassignedCount, Integer.class);
            result.put("unassignedTaskCount", unassignedTaskCount);

            logger.info("Counts → User: {}, Intern: {}, Unassigned: {}",
                    userTaskCount, internTaskCount, unassignedTaskCount);

        } catch (Exception e) {
            logger.error("Error fetching task counts: {}", e.getMessage(), e);
        }

        return result;
    }


    @Override
    public int getLoggedUserAndInternAssignedTaskCount(String username) {
        try {
            // user_id is VARCHAR -> fetch as String
            String getUserIdSql = "SELECT user_id FROM pmatm WHERE username = ?";
            String loggedUserId = jdbcTemplate.queryForObject(getUserIdSql, String.class, username);

            // Count tasks for logged user + interns
            String sql = """
            SELECT COUNT(*)
            FROM tasks t
            LEFT JOIN pmatm u ON t.assign_to = u.user_id
            WHERE t.status = 'A'
              AND t.terminated_by IS NULL
              AND t.terminated_on IS NULL
              AND (t.assign_to = ? OR u.userType = 'Intern')
        """;

            return jdbcTemplate.queryForObject(sql, Integer.class, loggedUserId);

        } catch (Exception e) {
            logger.error("Error counting logged user + intern tasks: {}", e.getMessage(), e);
            return 0;
        }
    }
    @Override
    public int getLoggedUserTasksCount(String username) {
        try {
            String sql = """
            SELECT COUNT(*)
            FROM user_management.tasks t
            LEFT JOIN user_management.pmatm u ON t.assign_to = u.user_id
            WHERE u.username = ?
               AND t.status = 'A'
              AND t.terminated_by IS NULL
              AND t.terminated_on IS NULL
        """;

            return jdbcTemplate.queryForObject(sql, Integer.class, username);

        } catch (Exception e) {
            logger.error("Error counting logged user tasks: {}", e.getMessage(), e);
            return 0;
        }
    }


    @Override
    public int getLoggedUserCompletedTasksCount(String username) {
        try {
            String sql = """
            SELECT COUNT(*)
            FROM user_management.tasks t
            LEFT JOIN user_management.pmatm u ON t.assign_to = u.user_id
            WHERE u.username = ?
              AND t.status = 'C'
              AND t.terminated_by IS NULL
              AND t.terminated_on IS NULL
        """;

            return jdbcTemplate.queryForObject(sql, Integer.class, username);

        } catch (Exception e) {
            logger.error("Error counting logged user completed tasks: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public int getLoggedUserActiveTasksCount(String username) {
        try {
            String sql = """
            SELECT COUNT(*)
            FROM user_management.tasks t
            LEFT JOIN user_management.pmatm u ON t.assign_to = u.user_id
            WHERE u.username = ?
              AND t.status = 'A'
              AND t.terminated_by IS NULL
              AND t.terminated_on IS NULL
        """;

            return jdbcTemplate.queryForObject(sql, Integer.class, username);

        } catch (Exception e) {
            logger.error("Error counting logged user active tasks: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public TaskDto findAssignTask(String taskId, String assignTo) {
        try {
            String sql = "SELECT * FROM assign_tasks WHERE task_id=? AND assign_to=?";
            return jdbcTemplate.queryForObject(sql, new Object[]{taskId, assignTo},
                    (rs, rowNum) -> {
                        TaskDto task = new TaskDto();
                        task.setTask_id(rs.getString("task_id"));
                        task.setAssign_to(rs.getString("assign_to"));
                        return task;
                    });
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void terminatePreviousTask(String taskId, String terminatedBy) {
        String updateSql = "UPDATE tasks SET terminated_by=?, terminated_on=CURRENT_DATE, status='D' WHERE task_id=? AND status='A'";
        jdbcTemplate.update(updateSql, terminatedBy, taskId);


    }

    @Override
    public boolean updateTaskStatusToOnHold(String taskId, String username) {
        try {
            // Update tasks table
            String updateTaskSql =
                    "UPDATE tasks SET status = 'D', added_by = ?, added_on = NOW() " +
                            "WHERE task_id = ? AND status IN ('A')";

            int updatedTasks = jdbcTemplate.update(updateTaskSql, username, taskId);

            // Update assign_tasks table
            String updateAssignSql =
                    "UPDATE assign_tasks SET status = 'D', added_by = ?, added_on = NOW() " +
                            "WHERE task_id = ? AND status IN ('A','P')";



            int updatedAssignTasks = jdbcTemplate.update(updateAssignSql, username, taskId);

            return (updatedTasks > 0 || updatedAssignTasks > 0);

        } catch (Exception e) {
            logger.error("Error updating task to on-hold: {}", e.getMessage(), e);
            return false;
        }
    }




    @Override
    public List<ProjectDto> getProjectsByIds(Set<Integer> projectIds) {

        if (projectIds == null || projectIds.isEmpty()) {
            return new ArrayList<>();
        }

        String sql = """
        SELECT project_id, project_name, start_date, expected_end_date
        FROM projects
        WHERE project_id IN (%s)
    """.formatted(
                projectIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","))
        );

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ProjectDto.class));
    }

    @Override
    public int getProjectIdByTask(String taskId) {
        String sql = "SELECT project_id FROM tasks WHERE task_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, taskId);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean areAllProjectTasksCompleted(int projectId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE project_id = ? AND status != 'C' AND terminated_by IS NULL";

        try {
            int count = jdbcTemplate.queryForObject(sql, Integer.class, projectId);
            return count == 0; // if zero → all tasks are completed
        } catch (Exception e) {
            logger.error("Error checking project tasks", e);
            return false;
        }
    }

    @Override
    public boolean markProjectAsCompleted(int projectId) {
        String sql = "UPDATE projects SET status = 'C' WHERE project_id = ?";

        try {
            return jdbcTemplate.update(sql, projectId) > 0;
        } catch (Exception e) {
            logger.error("Cannot update project status", e);
            return false;
        }
    }


}
