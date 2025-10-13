package com.jobspring.user.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 把进入 user-service 的请求头（网关已写入）透传给下游 Feign 请求：
 * - X-User-Id / X-User-Role 供 HeaderAuthFilter 使用
 * - Authorization（如果你也用 Bearer JWT）
 * - X-XSRF-TOKEN / Cookie（如果下游需要 CSRF，POST/PUT/DELETE 时用）
 */
@Component
public class PropagateAuthHeadersInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return; // 非 Web 请求线程（比如 @Async）时拿不到

        HttpServletRequest req = attrs.getRequest();

        // 网关或上游已经注入的头
        copy(req, template, "X-User-Id");
        copy(req, template, "X-User-Role");

        // 如还在用 Bearer JWT，这个也透传
        copy(req, template, HttpHeaders.AUTHORIZATION);

        // 非 GET 场景防 CSRF 时常用（下游若启用 CSRF 再打开）
        copy(req, template, "X-XSRF-TOKEN");
        copy(req, template, HttpHeaders.COOKIE);
    }

    private static void copy(HttpServletRequest req, RequestTemplate t, String name) {
        String v = req.getHeader(name);
        if (v != null && !v.isBlank()) {
            // Feign 的 header 是多值列表，这里简单设置单值
            t.header(name, v);
        }
    }
}