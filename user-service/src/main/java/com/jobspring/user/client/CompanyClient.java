package com.jobspring.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "company-service", contextId = "companyClient")
public interface CompanyClient {

    @GetMapping("/{companyId}/name")
    String getCompanyNameById(@PathVariable("companyId") Long companyId);
}
