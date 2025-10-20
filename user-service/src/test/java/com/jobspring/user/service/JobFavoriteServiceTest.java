package com.jobspring.user.service;

import com.jobspring.user.client.JobClient;
import com.jobspring.user.dto.FavoriteJobResponse;
import com.jobspring.user.dto.JobSummaryResponse;
import com.jobspring.user.entity.JobFavorite;
import com.jobspring.user.repository.JobFavoriteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobFavoriteServiceTest {

    @Mock JobFavoriteRepository favoriteRepository;
    @Mock JobClient jobClient;

    @InjectMocks JobFavoriteService service;

    @Test
    void add_should_save_when_job_exists_and_not_duplicate() {
        when(jobClient.existsById(2L)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndJobId(1L, 2L)).thenReturn(false);

        service.add(1L, 2L);

        ArgumentCaptor<JobFavorite> cap = ArgumentCaptor.forClass(JobFavorite.class);
        verify(favoriteRepository).save(cap.capture());
        assertThat(cap.getValue().getUserId()).isEqualTo(1L);
        assertThat(cap.getValue().getJobId()).isEqualTo(2L);
    }

    @Test
    void add_should_not_save_when_already_exists() {
        when(jobClient.existsById(2L)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndJobId(1L, 2L)).thenReturn(true);

        service.add(1L, 2L);

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void add_should_throw_when_job_not_found() {
        when(jobClient.existsById(2L)).thenReturn(false);

        assertThatThrownBy(() -> service.add(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Job not found");

        verifyNoInteractions(favoriteRepository);
    }

    @Test
    void remove_should_delegate_to_repo() {
        service.remove(9L, 8L);
        verify(favoriteRepository).deleteByUserIdAndJobId(9L, 8L);
    }

    @Test
    void isFavorited_should_delegate_to_repo() {
        when(favoriteRepository.existsByUserIdAndJobId(3L, 4L)).thenReturn(true);
        assertThat(service.isFavorited(3L, 4L)).isTrue();
    }

    @Test
    void list_should_map_fields_from_jobClient() {
        // given one favorite record
        JobFavorite fav = new JobFavorite();
        fav.setUserId(1L);
        fav.setJobId(100L);
        fav.setCreatedAt(LocalDateTime.of(2024, 6, 1, 12, 0));

        when(favoriteRepository.findByUserId(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(fav), PageRequest.of(0, 20), 1));

        JobSummaryResponse job = new JobSummaryResponse();
        job.setId(100L);
        job.setTitle("Backend Eng");
        job.setCompanyName("ACME");
        job.setLocation("SF");
        job.setEmploymentType(1);
        job.setStatus(0);
        when(jobClient.getJobSummary(100L)).thenReturn(job);

        // when
        Page<FavoriteJobResponse> page = service.list(1L, PageRequest.of(0, 20));

        // then
        assertThat(page.getTotalElements()).isEqualTo(1);
        FavoriteJobResponse r = page.getContent().get(0);
        assertThat(r.getJobId()).isEqualTo(100L);
        assertThat(r.getTitle()).isEqualTo("Backend Eng");
        assertThat(r.getCompany()).isEqualTo("ACME");
        assertThat(r.getLocation()).isEqualTo("SF");
        assertThat(r.getEmploymentType()).isEqualTo(1);
        assertThat(r.getStatus()).isEqualTo(0);
        assertThat(r.getFavoritedAt()).isEqualTo(fav.getCreatedAt());
    }
}
