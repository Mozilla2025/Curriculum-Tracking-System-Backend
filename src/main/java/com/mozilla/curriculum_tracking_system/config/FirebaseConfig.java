package com.mozilla.curriculum_tracking_system.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials.path}")
    private String credentialsPath;


    @Bean
    public Storage initializeFirebase() throws Exception {
        try {
            log.info("Initializing firebase with credentials: {}", credentialsPath);
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new ClassPathResource(credentialsPath).getInputStream());
            return StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();
        } catch (Exception e) {
            log.error("Error initializing firebase storage", e);
            throw e;
        }
    }
}
