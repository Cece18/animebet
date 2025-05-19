package com.crunchybet.betapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching
public class BetappApplication {

	public static void main(String[] args) {
		SpringApplication.run(BetappApplication.class, args);
	}

}
