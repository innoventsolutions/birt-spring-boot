package com.innoventsolutions.birt.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class CustomConfiguration extends AsyncConfigurerSupport {
	@Override
	public Executor getAsyncExecutor() {
		return new SimpleAsyncTaskExecutor();
	}

	@Override
	@Nullable
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (throwable, method, obj) -> {
			System.out.println("Exception Caught in Thread - " + Thread.currentThread().getName());
			System.out.println("Exception message - " + throwable.getMessage());
			System.out.println("Method name - " + method.getName());
			for (Object param : obj) {
				System.out.println("Parameter value - " + param);
			}
		};
	}
}