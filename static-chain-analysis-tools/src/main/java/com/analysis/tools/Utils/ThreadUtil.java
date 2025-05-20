package com.analysis.tools.Utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
public class ThreadUtil {
    public static <E, T> ThreadMapResult<E, T> getFutureResult(String taskName, Map<E, Future<T>> futures) throws InterruptedException {
        ThreadMapResult<E, T> result = new ThreadMapResult<>(new HashMap<>(), new HashMap<>());
        int cnt = 0;
        while (!futures.isEmpty()){
            futures = futures.entrySet().stream().filter(entry -> {
                if (entry.getValue().isDone()){
                    try {
                        result.getResult().put(entry.getKey(), entry.getValue().get());
                    } catch (Exception e) {
                        result.getFailedCase().put(entry.getKey(), e);
                    }
                    return false;
                }
                return true;
            }).collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                )
            );
            Thread.sleep(1000);
            if (cnt != futures.size()){
                log.info("{} 处理中，剩余任务数：{}", taskName, futures.size());
                cnt = futures.size();
            }
        }
        return result;
    }

    public static void waitForCompletion(String taskName, List<Future<?>> futures) throws InterruptedException {
        int cnt = futures.size();
        while(!futures.isEmpty()){
            futures = futures.stream().filter(future -> !future.isDone())
                    .collect(Collectors.toList());
            Thread.sleep(1000);
            if (cnt != futures.size()){
                log.info("{} 处理中，剩余任务数：{}", taskName, futures.size());
                cnt = futures.size();
            }
        }
    }

    @Data
    public static class ThreadMapResult<E, T> {
        private final Map<E, T> result;
        private final Map<E, Exception> failedCase;
    }
}
