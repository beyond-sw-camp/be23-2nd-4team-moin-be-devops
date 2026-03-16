package com.team4.moin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class
MoinApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoinApplication.class, args);
	}

}
