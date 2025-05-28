package com.mozilla.curriculum_tracking_system.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CurriculumConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
