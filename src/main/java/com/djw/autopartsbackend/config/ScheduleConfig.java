package com.djw.autopartsbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 定时任务配置
 * 启用定时任务支持，配置线程池
 * 
 * @author dengjiawen
 * @since 2026-02-17
 */
@Slf4j
@Configuration
@EnableScheduling
public class ScheduleConfig implements SchedulingConfigurer {

    /**
     * 定时任务线程池 Bean
     * 使用 ThreadPoolTaskScheduler 提供定时任务调度支持
     */
    @Bean(name = "scheduledTaskScheduler")
    public ThreadPoolTaskScheduler scheduledTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // 线程池大小
        scheduler.setPoolSize(5);
        
        // 线程名称前缀
        scheduler.setThreadNamePrefix("schedule-task-");
        
        // 等待任务全部完成后再关闭线程池
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        
        // 关闭时等待的时间（秒）
        scheduler.setAwaitTerminationSeconds(30);
        
        // 拒绝策略：由调用线程处理
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 初始化
        scheduler.initialize();
        
        log.info("定时任务线程池初始化完成：线程池大小=5");
        
        return scheduler;
    }

    /**
     * 配置定时任务
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(scheduledTaskScheduler());
    }
}
