package com.bistro.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

@Configuration
public class TracingConfig {

    @Bean
    public ContextPropagatingTaskDecorator contextPropagatingTaskDecorator(){
        return new ContextPropagatingTaskDecorator();
    }
}
