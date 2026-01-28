package com.example.mobitel.Service.Impl;

import com.example.mobitel.Controller.DashboardController;
import com.example.mobitel.Dao.DashboardDao;
import com.example.mobitel.Dao.LoginDao;
import com.example.mobitel.Dto.DashboardCountDto;
import com.example.mobitel.Dto.ResponseDto;
import com.example.mobitel.Service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    DashboardDao dashboardDao;

     @Autowired
     LoginDao loginDao;

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    public ResponseDto getDashboardCountsForLoggedUser(String username) {

        ResponseDto response = new ResponseDto();


        try {
            // Detect role from DB
            String userType = loginDao.getUserTypeByUsername(username);

            logger.info("Logged user = {}, userType = [{}]", username, userType);

            DashboardCountDto counts =
                    dashboardDao.getDashboardCounts(username, userType);


            response.setStatus("SUCCESS");
            response.setDescription("Dashboard counts fetched successfully");
            response.setContent(counts);
            response.setReturnCode("200");

        } catch (Exception e) {
            logger.error("Error fetching dashboard counts for user {}", username, e);
            response.setStatus("ERROR");
            response.setDescription("Failed to fetch dashboard counts");
            response.setReturnCode("500");
        }

        return response;
    }

}
