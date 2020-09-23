package com.orange.oss.osbreverseproxy;

import reactor.core.publisher.Hooks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		Hooks.onOperatorDebug();
		SpringApplication.run(Application.class, args);
	}


}
