package com.jobspring.application;


import com.jobspring.application.api.PdfController;
import com.jobspring.application.entity.PdfFile;
import com.jobspring.application.repository.PdfRepository;
import org.bson.types.Binary;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PdfController.class)
class PdfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PdfRepository pdfRepository;


    @Test
    void testUpload_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "PDFDATA".getBytes()
        );

        Mockito.when(pdfRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = mockMvc.perform(multipart("/upload")
                        .file(file)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(user("tester").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").exists())
                .andExpect(jsonPath("$.url").exists())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("publicId").contains("url");
    }


    @Test
    void testDownload_success() throws Exception {
        String publicId = UUID.randomUUID().toString().replace("-", "");
        PdfFile pdf = PdfFile.builder()
                .publicId(publicId)
                .filename("test.pdf")
                .contentType("application/pdf")
                .data(new Binary("PDFDATA".getBytes()))
                .uploadAt(Instant.now())
                .build();

        Mockito.when(pdfRepository.findByPublicId(eq(publicId))).thenReturn(Optional.of(pdf));

        mockMvc.perform(get("/download/{pid}", publicId)
                .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")))
                .andExpect(content().bytes("PDFDATA".getBytes()));
    }


    @Test
    void testDownload_notFound() throws Exception {
        String pid = "nonexistent";
        Mockito.when(pdfRepository.findByPublicId(eq(pid))).thenReturn(Optional.empty());

        mockMvc.perform(get("/download/{pid}", pid)
                .with(user("tester").roles("USER")))
                .andExpect(status().isNotFound());
    }
}
