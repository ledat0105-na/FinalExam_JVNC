package com.example.finalexam_jvnc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableJpaAuditing
public class FinalExamJvncApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(FinalExamJvncApplication.class, args);
        
//         PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
//         String password = "123456"; // Thay đổi password ở đây nếu cần
//         String hashedPassword = passwordEncoder.encode(password);
//
//         System.out.println("========================================");
//         System.out.println("Password Hash Code for '" + password + "':");
//         System.out.println(hashedPassword);
//         System.out.println("========================================");
//
    }

}
