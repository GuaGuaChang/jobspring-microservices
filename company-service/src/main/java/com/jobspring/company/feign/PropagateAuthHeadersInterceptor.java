package com.jobspring.company.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Component
public class PropagateAuthHeadersInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;

        HttpServletRequest req = attrs.getRequest();

        copy(req, template, "X-User-Id");
        copy(req, template, "X-User-Role");

        copy(req, template, HttpHeaders.AUTHORIZATION);

        copy(req, template, "X-XSRF-TOKEN");
        copy(req, template, HttpHeaders.COOKIE);
    }

    private static void copy(HttpServletRequest req, RequestTemplate t, String name) {
        String v = req.getHeader(name);
        if (v != null && !v.isBlank()) {
            t.header(name, v);
        }
    }
}