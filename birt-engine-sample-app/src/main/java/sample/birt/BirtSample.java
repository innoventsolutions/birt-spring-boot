package sample.birt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
// @EnableAutoConfiguration(exclude = { ErrorMvcAutoConfiguration.class })
public class BirtSample {
	public static void main(final String[] args) {
		SpringApplication.run(BirtSample.class, args);
	}
}
