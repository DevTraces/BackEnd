package com.devtraces.arterest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ArterestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArterestApplication.class, args);
	}

}
