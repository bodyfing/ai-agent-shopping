package com.gooshare;

import jakarta.annotation.PostConstruct;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gooshare.mapper")
public class GooShareApplication {
    public static void main(String[] args) {
        SpringApplication.run(GooShareApplication.class, args);
    }
}
