package com.innoventsolutions.birt.config;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
public class BirtAsyncConfiguration implements AsyncConfigurer {

//	private final TaskExecutionProperties taskExecutionProperties;

	public BirtAsyncConfiguration() {
//		this.taskExecutionProperties = taskExecutionProperties;
		log.info("BirtAsyncConfiguration init");
	}

	// ---------------> Tune parameters here
	@Override
	@Bean(name = "taskExecutor")
	public Executor getAsyncExecutor() {
		log.debug("Creating Async Task Executor");
		final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		/*
		 * executor.setCorePoolSize(taskExecutionProperties.getPool().getCoreSize());
		 * executor.setMaxPoolSize(taskExecutionProperties.getPool().getMaxSize());
		 * executor.setQueueCapacity(taskExecutionProperties.getPool().getQueueCapacity(
		 * ));
		 * executor.setThreadNamePrefix(taskExecutionProperties.getThreadNamePrefix());
		 */
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(3);
		executor.setThreadNamePrefix("run_report_pool");
		executor.initialize();

		return executor;
	}

	// ---------------> Use this task executor also for async rest methods
	@Bean
	protected WebMvcConfigurer webMvcConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void configureAsyncSupport(final AsyncSupportConfigurer configurer) {
				configurer.setTaskExecutor(getTaskExecutor());
			}
		};
	}

	@Bean
	protected ConcurrentTaskExecutor getTaskExecutor() {
		return new ConcurrentTaskExecutor(this.getAsyncExecutor());
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new BirtAsyncUncaughtExceptionHandler();
	}

	public class BirtAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {
		@Override
		public void handleUncaughtException(final Throwable ex, final Method method, final Object... params) {
			log.info("Method Name::" + method.getName());
			log.info("Exception occurred::" + ex);

		}
	}
}