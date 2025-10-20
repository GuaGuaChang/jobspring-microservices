package com.jobspring.user.controller;

import com.jobspring.user.client.AuthUserClient;
import com.jobspring.user.client.JobClient;
import com.jobspring.user.dto.*;
import com.jobspring.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class) // 明确只加载 UserController
@AutoConfigureMockMvc(addFilters = true)
// 防止 dev 配置里有 context-path / servlet-path 影响到测试 URL
@TestPropertySource(properties = {
        "server.servlet.context-path=",
        "spring.mvc.servlet.path="
})
@Import(UserControllerTest.MethodSecConfig.class) // 让 @PreAuthorize 生效（控制器里有两个方法需要）
class UserControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecConfig {}

    @Autowired MockMvc mockMvc;

    // 用 @MockBean 提供控制器依赖（如果你没写，会导致控制器创建失败，从而 404）
    @org.springframework.boot.test.mock.mockito.MockBean
    UserService userService;
    @org.springframework.boot.test.mock.mockito.MockBean
    AuthUserClient authUserClient;
    @org.springframework.boot.test.mock.mockito.MockBean
    JobClient jobClient;

    @Test
    void makeHr_ok_204() throws Exception {
        // stub：避免真正执行业务
        doNothing().when(userService).makeHr(123L, null);

        mockMvc.perform(post("/123/make-hr")
                        .with(user("u").roles("USER")) // 这个接口没要求 HR，只要已认证即可
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    // ---------- /search_user ----------
    @Test
    @WithMockUser
    @DisplayName("GET /search_user -> 200，传入的分页参数和排序会被转发到 AuthUserClient.search")
    void searchUsers_ok_200() throws Exception {
        // 用 mock 的 PageResponse，避免依赖 DTO 结构
        @SuppressWarnings("unchecked")
        PageResponse<UserDTO> mockResp = mock(PageResponse.class);
        when(mockResp.getContent()).thenReturn(List.of(new UserDTO()));
        when(mockResp.getPage()).thenReturn(1);          // page=1
        when(mockResp.getSize()).thenReturn(5);          // size=5
        when(mockResp.getTotalElements()).thenReturn(10L);

        when(authUserClient.search(eq("jack"), eq(1), eq(5), eq(List.of("id,ASC"))))
                .thenReturn(mockResp);

        mockMvc.perform(get("/search_user")
                        .param("q", "jack")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 断言 Page 的元数据，不依赖 UserDTO 字段
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(authUserClient, times(1))
                .search(eq("jack"), eq(1), eq(5), eq(List.of("id,ASC")));
        verifyNoMoreInteractions(authUserClient);
    }

    // ---------- /companies/{companyId}/jobs （需要 HR 角色 & CSRF） ----------
    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("POST /companies/{id}/jobs 非HR -> 403（即使有 CSRF）")
    void createJob_forbidden_ifNotHR() throws Exception {
        mockMvc.perform(post("/companies/1/jobs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(jobClient);
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("POST /companies/{id}/jobs HR 但缺少 CSRF -> 403")
    void createJob_missingCsrf_403() throws Exception {
        mockMvc.perform(post("/companies/1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(jobClient);
    }

    // 不断言请求体验证细节，单纯验证授权通过后能返回 200
    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("POST /companies/{id}/jobs HR + CSRF -> 200 并调用下游")
    void createJob_ok_callsDownstream() throws Exception {
        when(jobClient.createJob(eq(7L), any(JobCreateRequest.class)))
                .thenReturn(mock(JobResponse.class));// 空对象也可以序列化为 {}

        mockMvc.perform(post("/companies/7/jobs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(jobClient).createJob(eq(7L), any(JobCreateRequest.class));
        verifyNoMoreInteractions(jobClient);
    }

    // ---------- /companies/{companyId}/jobs/{jobId} （需要 HR 角色 & CSRF） ----------
    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("POST /companies/{cid}/jobs/{jid} HR + CSRF -> 200 并调用下游")
    void updateJob_ok_callsDownstream() throws Exception {
        when(jobClient.updateJob(eq(9L), eq(88L), any(JobUpdateRequest.class)))
                .thenReturn(mock(JobResponse.class));

        mockMvc.perform(post("/companies/9/jobs/88")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(jobClient).updateJob(eq(9L), eq(88L), any(JobUpdateRequest.class));
        verifyNoMoreInteractions(jobClient);
    }

    // ---------- /briefs:batch ----------
    @Test
    @WithMockUser
    @DisplayName("POST /briefs:batch -> 200 + 数组（含 CSRF）")
    void batchBrief_ok() throws Exception {
        List<UserBrief> mocked = List.of(
                new UserBrief(1L, "A"),
                new UserBrief(2L, "B")
        );
        when(userService.batchBrief(eq(List.of(1L, 2L)))).thenReturn(mocked);

        mockMvc.perform(post("/briefs:batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2]"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fullName").value("A"));

        verify(userService).batchBrief(eq(List.of(1L, 2L)));
        verifyNoMoreInteractions(userService);
    }

    // ---------- /users/{id} ----------
    @Test
    @WithMockUser
    @DisplayName("GET /users/{id} -> 200 并调用 userService.getUserById")
    void getUserById_ok() throws Exception {
        when(userService.getUserById(42L)).thenReturn(new UserDTO());

        mockMvc.perform(get("/users/42"))
                .andExpect(status().isOk());

        verify(userService).getUserById(42L);
        verifyNoMoreInteractions(userService);
    }
}
