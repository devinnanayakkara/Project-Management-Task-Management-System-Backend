package com.example.mobitel.Service;

import com.example.mobitel.Dto.DashboardCountDto;
import com.example.mobitel.Dto.ResponseDto;

public interface DashboardService {


     ResponseDto getDashboardCountsForLoggedUser(String username);
}
