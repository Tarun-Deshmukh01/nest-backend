 package com.tarun.nest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.boot.CommandLineRunner;
  import org.springframework.context.annotation.Bean;
  import org.springframework.core.env.Environment;
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;

@SpringBootApplication
public class NestBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NestBackendApplication.class, args);
    }

    private static final Logger logger = LoggerFactory.getLogger(NestBackendApplication.class);

    @Bean
    public CommandLineRunner printResolvedProperties(Environment env) {
        return args -> {
            // Print resolved datasource URL and username for local debugging (do not print passwords)
            String url = env.getProperty("spring.datasource.url");
            String user = env.getProperty("spring.datasource.username");
            logger.info("Resolved spring.datasource.url={}", url);
            logger.info("Resolved spring.datasource.username={}", user);
        };
    }

}