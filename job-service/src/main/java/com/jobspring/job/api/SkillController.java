package com.jobspring.job.api;

import com.jobspring.job.entity.Skill;
import com.jobspring.job.repository.SkillRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/skills")
public class SkillController {

    private final SkillRepository skillRepository;

    public SkillController(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    /**
     * 返回全部技能目录
     */
    @GetMapping
    public List<Skill> getAllSkills() {
        try {
            return skillRepository.findAll();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to fetch skills: " + e.getMessage());
        }
    }
}
