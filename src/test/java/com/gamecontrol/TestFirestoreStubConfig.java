package com.gamecontrol;

import com.google.cloud.firestore.Firestore;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestFirestoreStubConfig {

    @Bean
    public Firestore firestore() {
        return Mockito.mock(Firestore.class);
    }
}
