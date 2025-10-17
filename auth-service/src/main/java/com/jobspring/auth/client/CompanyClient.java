package com.jobspring.auth.client;


import com.jobspring.auth.dto.CompanyView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "company-service", url = "${COMPANY_BASE_URL:}")
public interface CompanyClient {

    @GetMapping("/{id}")
    CompanyView getById(@PathVariable("id") Long id);
}
