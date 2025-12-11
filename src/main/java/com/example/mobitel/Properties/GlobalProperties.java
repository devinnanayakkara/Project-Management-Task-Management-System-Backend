package com.example.mobitel.Properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
@Component@PropertySource("classpath:application.properties")
@Data
public class GlobalProperties {
    @Value("${mobitel.mf.frontend.url}")
    private String frontendURL;

    @Value("${mobitel.lDap.url}")
    private String lDapUrl;

    @Value("${mobitel.appCode}")
    private String appCode;

}

