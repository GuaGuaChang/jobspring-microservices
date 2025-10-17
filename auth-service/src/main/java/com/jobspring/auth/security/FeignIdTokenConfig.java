package com.jobspring.auth.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
@Profile("prod")
public class FeignIdTokenConfig {

    @Bean
    public RequestInterceptor idTokenInterceptor(
            @Value("${NOTIFICATION_BASE_URL}") String audience) {
        return tpl -> tpl.header("Authorization", "Bearer " + idToken(audience));
    }

    private String idToken(String audience) {
        try {
            var src = GoogleCredentials.getApplicationDefault();
            var creds = IdTokenCredentials.newBuilder()
                    .setIdTokenProvider((IdTokenProvider) src)
                    .setTargetAudience(audience)
                    .build();
            creds.refresh();
            return creds.getAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new IllegalStateException("Obtain ID token failed", e);
        }
    }
}