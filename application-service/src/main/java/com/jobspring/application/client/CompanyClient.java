package com.jobspring.application.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "company-service", url = "${COMPANY_BASE_URL:}")
public interface CompanyClient {

    @GetMapping("/hr/{hrUserId}/companyId")
    Long findCompanyIdByHr(@PathVariable("hrUserId") Long hrUserId);


    @GetMapping("/{companyId}/assert-hr")
    void assertHrInCompany(@RequestParam Long hrUserId, @PathVariable Long companyId);
}
