package com.analysis.admin.Service;

import com.analysis.core.Services.ScanService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanService {
    @Bean
    public ScanService createScanService(){
        return new ScanService();
    }
}
