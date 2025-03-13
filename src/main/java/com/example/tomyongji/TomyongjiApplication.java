package com.example.tomyongji;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
//@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class TomyongjiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TomyongjiApplication.class, args);
		Logger logger = (Logger) LoggerFactory.getLogger(TomyongjiApplication.class);
		logger.debug("This is a debug message.");
	}


}
