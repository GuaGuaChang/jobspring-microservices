package com.jobspring.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

@RestController
public class GwFallbackController {
    @RequestMapping("/__gw_fallback")
    public ResponseEntity<Map<String, Object>> fallback(ServerWebExchange ex) {
        return ResponseEntity.status(503).body(Map.of(
                "code", "GATEWAY_FALLBACK",
                "path", ex.getRequest().getPath().value(),
                "msg", "service temporarily unavailable"
        ));
    }
}