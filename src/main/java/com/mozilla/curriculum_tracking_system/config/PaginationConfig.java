package com.mozilla.curriculum_tracking_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableSpringDataWebSupport
public class PaginationConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setPageParameterName("page");
        resolver.setSizeParameterName("size");
        resolver.setOneIndexedParameters(false);
        resolver.setMaxPageSize(100);
        resolver.setFallbackPageable(PageRequest.of(0, 20, Sort.by("createdAt").descending()));
        resolvers.add(resolver);
    }

    @Bean
    public Pageable defaultPageable() {
        return PageRequest.of(0, 20, Sort.by("createdAt").descending());
    }
}
