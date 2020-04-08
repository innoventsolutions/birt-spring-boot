package sample.birt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@ComponentScan // (basePackages = "com.innoventsolutions.birt")
@EnableScheduling
public class BirtSample {
	public static void main(final String[] args) {
		SpringApplication.run(BirtSample.class, args);
	}
}
