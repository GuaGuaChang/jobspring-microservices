package com.jobspring.job.client;


import com.jobspring.job.dto.CompanyDTO;
import com.jobspring.job.dto.CompanyFavouriteResponse;
import com.jobspring.job.dto.CompanyResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "company-service")
public interface CompanyClient {

    @GetMapping("/{id}")
    CompanyResponse getCompanyById(@PathVariable("id") Long id);

    @GetMapping("/job-favourite/{id}")
    CompanyFavouriteResponse getCompanyFavouriteById(@PathVariable("id") Long id);

    @GetMapping("/search")
    List<Long> findCompanyIdsByName(@RequestParam("keyword") String keyword);

    @PostMapping("/batch")
    Map<Long, CompanyDTO> findByIds(@RequestBody List<Long> ids);
}
