package com.hsf.admin.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadConfiger {
    @Bean(name = "taskThreadPool")
    public ThreadPoolExecutor setThreadPoolExecutor(){
        return new ThreadPoolExecutor(
                50,
                100,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.DiscardPolicy()
        );
    }

    @Bean(name = "readClassBytesThreadPool")
    public ThreadPoolExecutor setReadClassBytesThreadPool(){
        return new ThreadPoolExecutor(
                20,
                30,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.DiscardPolicy()
        );
    }

    // cpu密集型任务线程池
    @Bean(name = "cpuTaskThreadPool")
    public ThreadPoolExecutor setCpuTaskThreadPool(){
        return new ThreadPoolExecutor(
                20,
                30,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.DiscardPolicy()
        );
    }
}