package com.jobspring.user.api;

import com.jobspring.user.client.AuthUserClient;
import com.jobspring.user.dto.ProfileRequestDTO;
import com.jobspring.user.dto.ProfileResponseDTO;
import com.jobspring.user.dto.ProfileUpdateResponseDTO;
import com.jobspring.user.dto.UserView;
import com.jobspring.user.service.ProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Resource
    MockMvc mockMvc;

    @MockBean
    ProfileService profileService;

    @MockBean
    AuthUserClient authUserClient;

    // ---------- GET /profile ----------

    @Test
    @DisplayName("GET /profile 没有 X-User-Id -> 401")
    @WithMockUser(roles = "CANDIDATE")
    void getProfile_missingHeader_should401() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason(org.hamcrest.Matchers.containsString("Missing X-User-Id")));
        verifyNoInteractions(profileService);
    }

    @Test
    @DisplayName("GET /profile AuthUserClient 抛错 -> 401")
    @WithMockUser(roles = "CANDIDATE")
    void getProfile_authClientThrows_should401() throws Exception {
        when(authUserClient.getCurrentUser("u1")).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/profile").header("X-User-Id", "u1"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason(org.hamcrest.Matchers.containsString("Auth-service validation failed")));

        verifyNoInteractions(profileService);
    }

    @Test
    @DisplayName("GET /profile AuthUserClient 返回 null -> 401")
    @WithMockUser(roles = "CANDIDATE")
    void getProfile_authClientReturnsNull_should401() throws Exception {
        when(authUserClient.getCurrentUser("u1")).thenReturn(null);

        mockMvc.perform(get("/profile").header("X-User-Id", "u1"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason(org.hamcrest.Matchers.containsString("User not found")));
        verifyNoInteractions(profileService);
    }

    @Test
    @DisplayName("GET /profile 成功 -> 200 + JSON")
    @WithMockUser(roles = "CANDIDATE")
    void getProfile_ok() throws Exception {
        UserView uv = new UserView();
        uv.setId(123L);
        when(authUserClient.getCurrentUser("u1")).thenReturn(uv);

        ProfileResponseDTO out = new ProfileResponseDTO();
        // 根据你的 DTO 结构，可给出最小字段；这里只要能序列化即可
        when(profileService.getCompleteProfile(123L)).thenReturn(out);

        mockMvc.perform(get("/profile").header("X-User-Id", "u1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        verify(profileService).getCompleteProfile(123L);
    }

    // ---------- POST /profile ----------

    @WithMockUser(roles = "CANDIDATE")
    @Test
    void postProfile_missingHeader_should401() throws Exception {
        mockMvc.perform(post("/profile")
                        .with(csrf()) // <— 关键：带上 CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        // visibility/level 在实体/DTO里是 Integer，别用字符串
                        .content("{\"profile\":{\"summary\":\"hi\",\"visibility\":0}}"))
                .andExpect(status().isUnauthorized()); // 现在会进入控制器抛 401
    }

    @Test
    @DisplayName("POST /profile AuthUserClient 抛错 -> 401")
    @WithMockUser(roles = "CANDIDATE")
    void postProfile_authClientThrows_should401() throws Exception {
        when(authUserClient.getCurrentUser("u1")).thenThrow(new RuntimeException("down"));

        mockMvc.perform(post("/profile")
                        .with(csrf().asHeader())                // ✅ 带上 CSRF
                        .header("X-User-Id", "u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profile\":{\"summary\":\"hi\",\"visibility\":0}}")) // ✅ visibility 用整数
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(profileService);
    }

    @Test
    @DisplayName("POST /profile AuthUserClient 返回 null -> 401")
    @WithMockUser(roles = "CANDIDATE")
    void postProfile_authClientNull_should401() throws Exception {
        when(authUserClient.getCurrentUser("u1")).thenReturn(null);

        mockMvc.perform(post("/profile")
                        .with(csrf().asHeader()) // ✅ 加 CSRF
                        .header("X-User-Id", "u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"profile":{"summary":"hi","visibility":0}}
            """))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason(org.hamcrest.Matchers.containsString("Invalid user ID")));

        verifyNoInteractions(profileService);
    }

    @Test
    @DisplayName("POST /profile 成功 -> 200 + JSON")
    @WithMockUser(roles = "CANDIDATE")
    void postProfile_ok() throws Exception {
        UserView uv = new UserView();
        uv.setId(123L);
        when(authUserClient.getCurrentUser("u1")).thenReturn(uv);

        when(profileService.createOrUpdateProfile(eq(123L), any(ProfileRequestDTO.class)))
                .thenReturn(new ProfileUpdateResponseDTO("success", "Profile updated successfully", 9L));

        mockMvc.perform(post("/profile")
                        .with(csrf().asHeader()) // ✅ 加 CSRF
                        .header("X-User-Id", "u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "profile":{"summary":"hi","visibility":0,"fileUrl":"x.pdf"},
                  "education":[],
                  "experience":[],
                  "skills":[]
                }
            """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.profileId").value(9L));

        verify(profileService).createOrUpdateProfile(eq(123L), any(ProfileRequestDTO.class));
    }
}
