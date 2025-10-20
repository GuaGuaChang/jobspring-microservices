package com.jobspring.user.api;

import com.jobspring.user.dto.FavoriteJobResponse;
import com.jobspring.user.service.JobFavoriteService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = JobFavoriteController.class)
@AutoConfigureMockMvc(addFilters = true)
class JobFavoriteControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean JobFavoriteService favoriteService;

    @Test
    void add_should_call_service_and_return_204() throws Exception {
        mockMvc.perform(post("/job_favorites/{jobId}", 10L)
                        .with(user("123").roles("CANDIDATE")) // Authentication#getName() = "123"
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(favoriteService).add(123L, 10L);
    }

    @Test
    void remove_should_call_service_and_return_204() throws Exception {
        mockMvc.perform(delete("/job_favorites/{jobId}", 99L)
                        .with(user("123").roles("CANDIDATE"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(favoriteService).remove(123L, 99L);
    }

    @Test
    void myFavorites_should_return_page_json() throws Exception {
        FavoriteJobResponse row = new FavoriteJobResponse();
        row.setJobId(5L);
        row.setTitle("Java Dev");
        row.setCompany("ACME");
        row.setLocation("NY");
        row.setEmploymentType(1);
        row.setStatus(0);
        row.setFavoritedAt(LocalDateTime.of(2024, 1, 2, 3, 4));

        Page<FavoriteJobResponse> page = new PageImpl<>(List.of(row));
        when(favoriteService.list(eq(123L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/job_favorites")
                        .with(user("123").roles("CANDIDATE")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].jobId").value(5))
                .andExpect(jsonPath("$.content[0].title").value("Java Dev"))
                .andExpect(jsonPath("$.content[0].company").value("ACME"));

        // 也可顺带验证 pageable 被传入
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(favoriteService).list(eq(123L), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
    }

    @Test
    void isFavorited_should_return_boolean_json() throws Exception {
        when(favoriteService.isFavorited(123L, 7L)).thenReturn(true);

        mockMvc.perform(get("/job_favorites/{jobId}/is-favorited", 7L)
                        .with(user("123").roles("CANDIDATE")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(true));   // 根节点就是布尔值

        verify(favoriteService).isFavorited(123L, 7L);
    }
}
