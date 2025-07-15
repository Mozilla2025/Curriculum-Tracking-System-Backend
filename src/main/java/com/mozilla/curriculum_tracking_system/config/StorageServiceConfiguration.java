package com.mozilla.curriculum_tracking_system.config;


import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;


/**
 * Configuration class for S3 storage services
 * Provides additional S3-related beans and validation
 */
@Configuration
@Slf4j
public class StorageServiceConfiguration {

    @Value("${aws.s3.bucket}")
    private String defaultBucket;

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${app.storage.max-file-size:52428800}") // 50MB default
    private long maxFileSize;

    @Value("${app.storage.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Value("${app.storage.cleanup.retention-days:90}")
    private int retentionDays;

    /**
     * S3 Presigner bean for generating pre-signed URLs
     */
    @Bean
    @Primary
    public S3Presigner s3Presigner() {
        log.info("Creating S3 Presigner for region: {}", awsRegion);
        return S3Presigner.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    /**
     * Storage configuration properties
     */
    @Bean
    public StorageProperties storageProperties() {
        StorageProperties properties = new StorageProperties();
        properties.setDefaultBucket(defaultBucket);
        properties.setMaxFileSize(maxFileSize);
        properties.setCleanupEnabled(cleanupEnabled);
        properties.setRetentionDays(retentionDays);
        properties.setAwsRegion(awsRegion);
        return properties;
    }

    @PostConstruct
    public void validateConfiguration() {
        log.info("Validating storage configuration...");

        if (defaultBucket == null || defaultBucket.trim().isEmpty()) {
            throw new IllegalStateException("AWS S3 bucket name must be configured");
        }

        if (awsRegion == null || awsRegion.trim().isEmpty()) {
            throw new IllegalStateException("AWS region must be configured");
        }

        if (maxFileSize <= 0) {
            throw new IllegalStateException("Maximum file size must be positive");
        }

        log.info("Storage configuration validated successfully:");
        log.info("  Default bucket: {}", defaultBucket);
        log.info("  AWS region: {}", awsRegion);
        log.info("  Max file size: {} MB", maxFileSize / (1024 * 1024));
        log.info("  Cleanup enabled: {}", cleanupEnabled);
        log.info("  Retention days: {}", retentionDays);
    }

    /**
     * Storage configuration properties class
     */
    @Setter
    @Getter
    public static class StorageProperties {
        private String defaultBucket;
        private long maxFileSize;
        private boolean cleanupEnabled;
        private int retentionDays;
        private String awsRegion;

    }
}
