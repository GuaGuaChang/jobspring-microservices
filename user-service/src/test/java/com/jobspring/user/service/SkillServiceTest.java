package com.jobspring.user.service;

import com.jobspring.user.client.JobClient;
import com.jobspring.user.dto.SkillDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SkillServiceTest {

    private static SkillDTO skill(long id, String name, String category) {
        SkillDTO s = new SkillDTO();
        s.setId(id);
        s.setName(name);
        s.setCategory(category);
        return s;
    }


    @Test
    @DisplayName("getAllSkills() 应调用 JobClient 并返回结果")
    void getAllSkills_ok() {
        JobClient jobClient = Mockito.mock(JobClient.class);
        SkillService service = new SkillService(jobClient);

        List<SkillDTO> mockList = List.of(
                skill(1L, "Java", "Backend"),
                skill(2L, "React", "Frontend")
        );
        when(jobClient.getAllSkills()).thenReturn(mockList);

        List<SkillDTO> result = service.getAllSkills();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Java");
        verify(jobClient, times(1)).getAllSkills();
        verifyNoMoreInteractions(jobClient);
    }

    @Test
    @DisplayName("getAllSkills() 遇到下游异常应直接抛出")
    void getAllSkills_downstreamThrows() {
        JobClient jobClient = Mockito.mock(JobClient.class);
        SkillService service = new SkillService(jobClient);

        when(jobClient.getAllSkills()).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, service::getAllSkills);
        verify(jobClient).getAllSkills();
    }
}
