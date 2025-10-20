package com.jobspring.user.api;

import com.jobspring.user.dto.SkillDTO;
import com.jobspring.user.service.SkillService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SkillController.class)
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SkillService skillService;

    private static SkillDTO skill(long id, String name, String category) {
        SkillDTO s = new SkillDTO();
        s.setId(id);
        s.setName(name);
        s.setCategory(category);
        return s;
    }


    @Test
    @WithMockUser // <— 关键：提供一个已认证的用户
    @DisplayName("GET /skills 成功 -> 200 + JSON 数组")
    void getAllSkills_ok() throws Exception {

        SkillDTO s1 = new SkillDTO();
        s1.setId(1L);
        s1.setName("Java");
        s1.setCategory("Backend");

        SkillDTO s2 = new SkillDTO();
        s2.setId(2L);
        s2.setName("React");
        s2.setCategory("Frontend");

        List<SkillDTO> mockList = List.of(s1, s2);

        when(skillService.getAllSkills()).thenReturn(mockList);

        mockMvc.perform(get("/skills"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[0].category").value("Backend"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("React"));

        verify(skillService).getAllSkills();
        verifyNoMoreInteractions(skillService);
    }

    @Test
    @WithMockUser // 关键：否则会 401，永远到不了 controller
    @DisplayName("GET /skills 服务抛异常 -> 500")
    void getAllSkills_serviceThrows_500() throws Exception {
        when(skillService.getAllSkills()).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/skills"))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason(org.hamcrest.Matchers.containsString("Failed to fetch skills")));

        verify(skillService, times(1)).getAllSkills();
        verifyNoMoreInteractions(skillService);
    }
}
