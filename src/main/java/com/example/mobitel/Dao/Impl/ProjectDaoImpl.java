package com.example.mobitel.Dao.Impl;

import com.example.mobitel.Dao.ProjectDao;
import com.example.mobitel.Dto.ProjectDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ProjectDaoImpl implements ProjectDao {


    private static final Logger logger = LoggerFactory.getLogger(ProjectDaoImpl.class);

    @Autowired
    private  JdbcTemplate jdbcTemplate;



    @Override
    public ProjectDto findProject(String project_name, String status) {
        String sql = "SELECT * FROM projects " +
                "WHERE project_name = ? " +
                "AND status = 'P' " +
                "AND terminated_by IS NULL " +
                "AND terminated_on IS NULL";

          ProjectDto projectDto = null;

        try{
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql,project_name);

            if(sqlRowSet.next()){

                projectDto = new ProjectDto();

                projectDto.setProject_name(sqlRowSet.getString("project_name"));
                projectDto.setProject_description(sqlRowSet.getString("project_description"));
                projectDto.setStart_date(sqlRowSet.getDate("start_date").toLocalDate());
                projectDto.setExpected_end_date(sqlRowSet.getDate("expected_end_date").toLocalDate());
                projectDto.setActual_end_date(sqlRowSet.getDate("actual_end_date").toLocalDate());
                projectDto.setAdded_by(sqlRowSet.getString("added_by"));
                projectDto.setAdded_on(sqlRowSet.getDate("added_on").toLocalDate());
                projectDto.setStatus(sqlRowSet.getString("status"));

            }
        } catch (Exception e) {
           logger.error("No Projects Found with project_name: {} and status: {} : {}",project_name, status, e.getMessage());
        }

        return projectDto;
    }

    @Override
    public int AddProject(ProjectDto request) {
        try {
            String sql = "INSERT INTO projects" + "(project_name,project_description,start_date,expected_end_date,actual_end_date,added_by,added_on,status)" + "VALUES(?,?,?,?,?,?,?,?)";

            return jdbcTemplate.update(sql,

                    request.getProject_name(),
                    request.getProject_description(),
                    request.getStart_date(),
                    request.getExpected_end_date(),
                    request.getActual_end_date(),
                    request.getAdded_by(),
                    request.getAdded_on(),
                    request.getStatus()

            );


        } catch (Exception e) {
            logger.error("Error adding a Project: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public ProjectDto searchProject(String project_name, String status) {
        String sql = "SELECT * FROM projects " +
                "WHERE project_name = ? " +
                "AND status = ? " +
                "AND terminated_by IS NULL " +
                "AND terminated_on IS NULL";

        ProjectDto projectDto = null;

        try{
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql,project_name,status);

            if(sqlRowSet.next()){

                projectDto = new ProjectDto();
                projectDto.setProject_id(sqlRowSet.getInt("project_id"));
                projectDto.setProject_name(sqlRowSet.getString("project_name"));
                projectDto.setProject_description(sqlRowSet.getString("project_description"));
                projectDto.setStart_date(sqlRowSet.getDate("start_date").toLocalDate());
                projectDto.setExpected_end_date(sqlRowSet.getDate("expected_end_date").toLocalDate());
                java.sql.Date actualEndDate = sqlRowSet.getDate("actual_end_date");
                if (actualEndDate != null) {
                    projectDto.setActual_end_date(actualEndDate.toLocalDate());
                } else {
                    projectDto.setActual_end_date(null);
                }

                projectDto.setAdded_by(sqlRowSet.getString("added_by"));
                projectDto.setAdded_on(sqlRowSet.getDate("added_on").toLocalDate());
                projectDto.setStatus(sqlRowSet.getString("status"));

            }
        } catch (Exception e) {
            logger.error("No Projects Found with project_name: {} and status: {} : {}",project_name, status, e.getMessage());
        }

        return projectDto;
    }

//    @Override
//    public List<ProjectDto> viewProjects() {
//        String sql = "SELECT * FROM projects " +
//                "WHERE status = ? " +
//                "AND terminated_by IS NULL " +
//                "AND terminated_on IS NULL";
//
//
//
//                List<ProjectDto> projects = new ArrayList<>();
//
//        try {
//            // Use "P" as the status
//            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, "P");
//
//            while (sqlRowSet.next()) {
//                ProjectDto projectDto = new ProjectDto();
//
//                projectDto.setProject_id(sqlRowSet.getInt("project_id"));
//                projectDto.setProject_name(sqlRowSet.getString("project_name"));
//                projectDto.setProject_description(sqlRowSet.getString("project_description"));
//
//                // Safe date conversion for nullable dates
//                java.sql.Date startDate = sqlRowSet.getDate("start_date");
//                if (startDate != null) projectDto.setStart_date(startDate.toLocalDate());
//
//                java.sql.Date expectedEndDate = sqlRowSet.getDate("expected_end_date");
//                if (expectedEndDate != null) projectDto.setExpected_end_date(expectedEndDate.toLocalDate());
//
//                java.sql.Date actualEndDate = sqlRowSet.getDate("actual_end_date");
//                if (actualEndDate != null) projectDto.setActual_end_date(actualEndDate.toLocalDate());
//
//                java.sql.Date addedOn = sqlRowSet.getDate("added_on");
//                if (addedOn != null) projectDto.setAdded_on(addedOn.toLocalDate());
//
//                projectDto.setAdded_by(sqlRowSet.getString("added_by"));
//                projectDto.setStatus(sqlRowSet.getString("status"));
//
//                projects.add(projectDto);
//            }
//        } catch (Exception e) {
//            logger.error("Cannot fetch pending projects", e);
//        }
//
//        return projects;
//    }

    @Override
    public List<ProjectDto> viewProjects() {

        String sql = "SELECT p.project_id, p.project_name, p.project_description, " +
                "p.start_date, p.expected_end_date, p.actual_end_date, " +
                "p.added_by, p.added_on, p.status, s.status_name " +
                "FROM projects p " +
                "JOIN status s ON p.status = s.status_id " +
                "WHERE p.terminated_by IS NULL " +
                "AND p.terminated_on IS NULL";

        List<ProjectDto> projects = new ArrayList<>();

        try {
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);

            while (sqlRowSet.next()) {
                ProjectDto projectDto = new ProjectDto();

                projectDto.setProject_id(sqlRowSet.getInt("project_id"));
                projectDto.setProject_name(sqlRowSet.getString("project_name"));
                projectDto.setProject_description(sqlRowSet.getString("project_description"));

                // Safe date conversion for nullable dates
                java.sql.Date startDate = sqlRowSet.getDate("start_date");
                if (startDate != null) projectDto.setStart_date(startDate.toLocalDate());

                java.sql.Date expectedEndDate = sqlRowSet.getDate("expected_end_date");
                if (expectedEndDate != null) projectDto.setExpected_end_date(expectedEndDate.toLocalDate());

                java.sql.Date actualEndDate = sqlRowSet.getDate("actual_end_date");
                if (actualEndDate != null) projectDto.setActual_end_date(actualEndDate.toLocalDate());

                java.sql.Date addedOn = sqlRowSet.getDate("added_on");
                if (addedOn != null) projectDto.setAdded_on(addedOn.toLocalDate());

                projectDto.setAdded_by(sqlRowSet.getString("added_by"));
                projectDto.setStatus(sqlRowSet.getString("status"));          // P/C/A/D
                projectDto.setStatus_name(sqlRowSet.getString("status_name")); // Pending, Completed, ..

                projects.add(projectDto);
            }

        } catch (Exception e) {
            logger.error("Cannot fetch projects", e);
        }

        return projects;
    }



    public int countPendingProjects() {
        String sql = "SELECT COUNT(*) FROM projects WHERE status = ? AND terminated_by IS NULL AND terminated_on IS NULL";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, new Object[]{"P"}, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("Cannot count pending projects", e);
            return 0;
        }
    }
}
