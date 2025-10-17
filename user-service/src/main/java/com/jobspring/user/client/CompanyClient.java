package com.jobspring.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "company-service", contextId = "companyClient", url = "${COMPANY_BASE_URL:}")
public interface CompanyClient {
    @GetMapping("/{companyId}/name")
    String getCompanyNameById(@PathVariable("companyId") Long companyId);

    @GetMapping("/hr/{hrUserId}/companyId")
    Long findCompanyIdByHr(@PathVariable Long hrUserId);
}
