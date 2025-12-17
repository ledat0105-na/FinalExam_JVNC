package com.example.finalexam_jvnc.config;

import jakarta.servlet.ServletContextListener;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations("file:uploads/products/");
    }
    
    @Bean
    public ServletListenerRegistrationBean<ServletContextListener> sessionListener() {
        ServletListenerRegistrationBean<ServletContextListener> registrationBean = 
            new ServletListenerRegistrationBean<>();
        registrationBean.setListener(new SessionListener());
        return registrationBean;
    }
}
