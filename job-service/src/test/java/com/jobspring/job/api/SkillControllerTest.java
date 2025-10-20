package com.jobspring.job.api;

import com.jobspring.job.entity.Skill;
import com.jobspring.job.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.hasSize;

/**
 * 只启动 SkillController 的 Web 层，不加载整个上下文
 */
@WebMvcTest(SkillController.class)
@AutoConfigureMockMvc(addFilters = false) // 若工程开启了 Spring Security，关闭过滤器以免 401/403 影响测试
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SkillRepository skillRepository;

    private static Skill skill(long id, String name) {
        Skill s = new Skill();
        s.setId(id);
        s.setName(name);
        return s;
    }

    @Test
    void getAllSkills_should_return_list() throws Exception {
        // given
        List<Skill> data = List.of(
                skill(1L, "Java"),
                skill(2L, "Spring Boot")
        );
        given(skillRepository.findAll()).willReturn(data);

        // when & then
        mockMvc.perform(get("/skills"))
                .andExpect(status().isOk())
                // 数组长度
                .andExpect(jsonPath("$", hasSize(2)))
                // 简单校验几个字段（若实体里字段不同，可删掉或改成已有字段）
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Spring Boot"));
    }

    @Test
    void getAllSkills_when_repo_throws_should_return_500() throws Exception {
        // given
        when(skillRepository.findAll()).thenThrow(new RuntimeException("db down"));

        mockMvc.perform(get("/skills"))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason(containsString("Failed to fetch skills")));
    }
}
