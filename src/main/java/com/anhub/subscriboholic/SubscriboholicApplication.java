package com.anhub.subscriboholic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class SubscriboholicApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubscriboholicApplication.class, args);
	}

}
