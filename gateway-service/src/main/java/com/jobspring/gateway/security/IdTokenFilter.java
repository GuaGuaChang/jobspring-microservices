package com.jobspring.gateway.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Profile("cloud")
public class IdTokenFilter implements GlobalFilter, Ordered {

    @Value("${AUTH_BASE_URL}")
    String authAudience;

    @Override
    public Mono<Void> filter(ServerWebExchange ex, GatewayFilterChain chain) {
        String path = ex.getRequest().getPath().value();
        if (path.startsWith("/api/auth/")) {
            return Mono.fromCallable(() -> token(authAudience))
                    .flatMap(t -> chain.filter(ex.mutate().request(
                            ex.getRequest().mutate()
                                    .headers(h -> h.set("Authorization", "Bearer " + t))
                                    .build()
                    ).build()));
        }
        return chain.filter(ex);
    }

    private String token(String audience) throws Exception {
        var src = GoogleCredentials.getApplicationDefault();
        var id = IdTokenCredentials.newBuilder()
                .setIdTokenProvider((IdTokenProvider) src)
                .setTargetAudience(audience)
                .build();
        id.refresh();
        return id.getAccessToken().getTokenValue();
    }

    @Override
    public int getOrder() {
        return -10;
    }
}