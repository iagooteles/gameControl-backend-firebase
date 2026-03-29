package com.gamecontrol.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Profile("!test")
public class FirebaseConfig {

    @Bean
    public Firestore firestore(
            @Value("${firebase.credentials.path:}") String credentialsPath,
            @Value("${firebase.project-id:}") String projectId
    ) throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions.Builder builder = FirebaseOptions.builder();
            if (StringUtils.hasText(credentialsPath)) {
                Path keyPath = Paths.get(credentialsPath.trim());
                if (!keyPath.isAbsolute()) {
                    keyPath = Paths.get(System.getProperty("user.dir", ".")).resolve(keyPath);
                }
                try (InputStream in = new FileInputStream(keyPath.toFile())) {
                    builder.setCredentials(GoogleCredentials.fromStream(in));
                }
            } else {
                builder.setCredentials(GoogleCredentials.getApplicationDefault());
            }
            if (StringUtils.hasText(projectId)) {
                builder.setProjectId(projectId.trim());
            }
            FirebaseApp.initializeApp(builder.build());
        }
        return FirestoreClient.getFirestore();
    }
}
