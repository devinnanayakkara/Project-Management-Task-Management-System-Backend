package com.example.mobitel.Dao;

import com.example.mobitel.Dto.DashboardCountDto;

public interface DashboardDao {


    DashboardCountDto getDashboardCounts(String username, String userType);
}
