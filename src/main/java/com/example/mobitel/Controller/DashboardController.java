package com.example.mobitel.Controller;

import com.example.mobitel.Config.Utility.JwtUtil;
import com.example.mobitel.Dao.Impl.DashboardDaoImpl;
import com.example.mobitel.Dto.DashboardCountDto;
import com.example.mobitel.Dto.ResponseDto;
import com.example.mobitel.Service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(DashboardDaoImpl.class);

    @GetMapping("/counts")
    public ResponseEntity<ResponseDto> getDashboardCounts(HttpServletRequest request) {

        ResponseDto response = new ResponseDto();

        try {
            //  Extract JWT token
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);

            //  Extract logged-in username
            String username = jwtUtil.extractUsername(token);

            //  Fetch dashboard counts for logged user
            response = dashboardService.getDashboardCountsForLoggedUser(username);

            //  Return response
            if ("SUCCESS".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }

        } catch (Exception e) {
            logger.error("Error fetching dashboard counts: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setDescription("Internal Server Error while fetching dashboard counts");
            response.setReturnCode("500");
            return ResponseEntity.status(500).body(response);
        }
    }
}

