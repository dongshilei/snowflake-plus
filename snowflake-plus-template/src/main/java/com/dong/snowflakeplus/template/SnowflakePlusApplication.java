package com.dong.snowflakeplus.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.dong")
public class SnowflakePlusApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnowflakePlusApplication.class, args);
    }

}
