package org.bzk.documentserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author 2023-02-26 21:56 ly
 **/
@SpringBootApplication
@EnableScheduling
public class WebApplication {

    public static void main(String[] args) {
        new SpringApplication(WebApplication.class)
                .run(args);
    }
}
