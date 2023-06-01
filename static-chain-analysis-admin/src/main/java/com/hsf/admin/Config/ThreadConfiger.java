package com.hsf.admin.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadConfiger {
    @Bean
    public ThreadPoolExecutor setThreadPoolExecutor(){
        return new ThreadPoolExecutor(
            50,
            100,
            30,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadPoolExecutor.DiscardPolicy()
        );
    }
}
