package com.mozilla.curriculum_tracking_system.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mozilla.curriculum_tracking_system.constants.CacheConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    @Value("${app.cache.ttl.departments:1800}")
    private int departmentCacheTtl;

    @Value("${app.cache.ttl.schools:3600}")
    private int schoolCacheTtl;

    @Value("${app.cache.ttl.curriculums:2700}")
    private int curriculumCacheTtl;

    @Value("${app.cache.ttl.curriculum-stats:300}")
    private int curriculumStatsCacheTtl;

    @Value("${app.cache.ttl.general:900}")
    private int generalCacheTtl;

    private ObjectMapper createRedisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return mapper;
    }

    @Bean("cacheValueSerializer")
    public GenericJackson2JsonRedisSerializer cacheValueSerializer() {
        return new GenericJackson2JsonRedisSerializer(createRedisObjectMapper());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(cacheValueSerializer());
        template.setHashValueSerializer(cacheValueSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(generalCacheTtl))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(cacheValueSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = createCacheConfigurations(defaultConfig);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    private Map<String, RedisCacheConfiguration> createCacheConfigurations(RedisCacheConfiguration defaultConfig) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        addDepartmentCacheConfigurations(cacheConfigurations, defaultConfig);
        addSchoolCacheConfigurations(cacheConfigurations, defaultConfig);
        addCurriculumCacheConfigurations(cacheConfigurations, defaultConfig);

        return cacheConfigurations;
    }

    private void addDepartmentCacheConfigurations(Map<String, RedisCacheConfiguration> cacheConfigurations,
                                                  RedisCacheConfiguration defaultConfig) {
        Duration departmentTtl = Duration.ofSeconds(departmentCacheTtl);

        cacheConfigurations.put(CacheConstants.DEPARTMENTS, defaultConfig.entryTtl(departmentTtl));
        cacheConfigurations.put(CacheConstants.DEPARTMENT_BY_ID, defaultConfig.entryTtl(departmentTtl));
        cacheConfigurations.put(CacheConstants.DEPARTMENTS_BY_SCHOOL, defaultConfig.entryTtl(departmentTtl));
        cacheConfigurations.put(CacheConstants.DEPARTMENTS_SEARCH, defaultConfig.entryTtl(departmentTtl));
        cacheConfigurations.put(CacheConstants.DEPARTMENTS_SEARCH_BY_SCHOOL, defaultConfig.entryTtl(departmentTtl));
        cacheConfigurations.put(CacheConstants.DEPARTMENT_COUNT_BY_SCHOOL, defaultConfig.entryTtl(departmentTtl));
        cacheConfigurations.put(CacheConstants.DEPARTMENT_EXISTS, defaultConfig.entryTtl(departmentTtl));
    }

    private void addSchoolCacheConfigurations(Map<String, RedisCacheConfiguration> cacheConfigurations,
                                              RedisCacheConfiguration defaultConfig) {
        Duration schoolTtl = Duration.ofSeconds(schoolCacheTtl);
        cacheConfigurations.put(CacheConstants.SCHOOLS, defaultConfig.entryTtl(schoolTtl));
        cacheConfigurations.put(CacheConstants.SCHOOL_BY_ID, defaultConfig.entryTtl(schoolTtl));
        cacheConfigurations.put(CacheConstants.SCHOOL_EXISTS, defaultConfig.entryTtl(schoolTtl));
    }

    private void addCurriculumCacheConfigurations(Map<String, RedisCacheConfiguration> cacheConfigurations,
                                                  RedisCacheConfiguration defaultConfig) {
        Duration curriculumTtl = Duration.ofSeconds(curriculumCacheTtl);
        Duration curriculumStatsTtl = Duration.ofSeconds(curriculumStatsCacheTtl);

        cacheConfigurations.put(CacheConstants.CURRICULUMS, defaultConfig.entryTtl(curriculumTtl));
        cacheConfigurations.put(CacheConstants.CURRICULUM_BY_ID, defaultConfig.entryTtl(curriculumTtl));
        cacheConfigurations.put(CacheConstants.CURRICULUMS_BY_SCHOOL, defaultConfig.entryTtl(curriculumTtl));
        cacheConfigurations.put(CacheConstants.CURRICULUMS_BY_DEPARTMENT, defaultConfig.entryTtl(curriculumTtl));
        cacheConfigurations.put(CacheConstants.CURRICULUMS_BY_ACADEMIC_LEVEL, defaultConfig.entryTtl(curriculumTtl));
        cacheConfigurations.put(CacheConstants.CURRICULUMS_SEARCH, defaultConfig.entryTtl(curriculumTtl));
        cacheConfigurations.put(CacheConstants.CURRICULUMS_EXPIRING_SOON, defaultConfig.entryTtl(curriculumTtl));

        cacheConfigurations.put(CacheConstants.CURRICULUM_EXISTS_BY_NAME_DEPT_LEVEL, defaultConfig.entryTtl(curriculumTtl));
        cacheConfigurations.put(CacheConstants.CURRICULUM_EXISTS_BY_CODE, defaultConfig.entryTtl(curriculumTtl));

        cacheConfigurations.put(CacheConstants.CURRICULUM_STATS, defaultConfig.entryTtl(curriculumStatsTtl));
    }
}