package com.hsf.admin.Service;

import com.hsf.core.Services.ScanService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanService {
    @Bean
    public ScanService createScanService(){
        return new ScanService();
    }
}
