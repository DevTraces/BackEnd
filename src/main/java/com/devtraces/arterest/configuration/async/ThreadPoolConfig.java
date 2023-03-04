package com.devtraces.arterest.configuration.async;

import com.devtraces.arterest.common.constant.CommonConstant;
import java.util.concurrent.Executor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig extends AsyncConfigurerSupport {

    @Bean
    public Executor taskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int n = Runtime.getRuntime().availableProcessors();

        executor.setCorePoolSize(2*n);
        executor.setMaxPoolSize(2*n);
        executor.setQueueCapacity(CommonConstant.NUMBER_OF_EXPECTED_MAX_REQUEST *3);
        executor.setThreadNamePrefix("GithubLookup-");
        executor.initialize();
        return executor;
    }


    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

}
