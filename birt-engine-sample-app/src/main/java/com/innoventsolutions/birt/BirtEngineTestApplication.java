package com.innoventsolutions.birt;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// if this annotation is not commented out then run will execute
@SpringBootApplication
public class BirtEngineTestApplication implements CommandLineRunner {


	public static void main(final String[] args) {
		SpringApplication.run(BirtEngineTestApplication.class, args);
	}

	@Override
	public void run(final String... args) throws Exception {
	}
}
