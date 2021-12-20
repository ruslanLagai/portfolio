package com.home.project.portfolio.utils;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Class to help running parallel threads
 */
@Log4j2
public class ExecutorServiceUtils {

    public static void execute(Runnable task, ExecutorService executor) {
        try {
            executor.execute(task);
        } finally {
            executor.shutdown();
            try {
                if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
}
