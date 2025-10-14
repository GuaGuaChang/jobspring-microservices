package com.jobspring.user.service;

import com.jobspring.user.client.JobClient;
import com.jobspring.user.dto.SkillDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillService {

    private final JobClient jobClient;

    public SkillService(JobClient jobClient) {
        this.jobClient = jobClient;
    }

    public List<SkillDTO> getAllSkills() {
        return jobClient.getAllSkills();
    }
}
