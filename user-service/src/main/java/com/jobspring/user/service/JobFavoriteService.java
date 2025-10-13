package com.jobspring.user.service;

import com.jobspring.user.client.JobClient;
import com.jobspring.user.dto.FavoriteJobResponse;
import com.jobspring.user.dto.JobSummaryResponse;
import com.jobspring.user.entity.JobFavorite;
import com.jobspring.user.repository.JobFavoriteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobFavoriteService {

    private final JobFavoriteRepository favoriteRepository;
    private final JobClient jobClient;

    @Transactional
    public void add(Long userId, Long jobId) {
        // 调 job-service 校验
        Boolean exists = jobClient.existsById(jobId);
        if (Boolean.FALSE.equals(exists)) {
            throw new EntityNotFoundException("Job not found");
        }

        // 幂等插入
        if (!favoriteRepository.existsByUserIdAndJobId(userId, jobId)) {
            JobFavorite fav = new JobFavorite();
            fav.setUserId(userId);
            fav.setJobId(jobId);
            favoriteRepository.save(fav);
        }
    }

    @Transactional
    public void remove(Long userId, Long jobId) {
        favoriteRepository.deleteByUserIdAndJobId(userId, jobId);
    }

    public Page<FavoriteJobResponse> list(Long userId, Pageable pageable) {
        return favoriteRepository.findByUserId(userId, pageable)
                .map(f -> {
                    JobSummaryResponse job = jobClient.getJobSummary(f.getJobId());
                    FavoriteJobResponse r = new FavoriteJobResponse();
                    r.setJobId(job.getId());
                    r.setTitle(job.getTitle());
                    r.setCompany(job.getCompanyName());
                    r.setLocation(job.getLocation());
                    r.setEmploymentType(job.getEmploymentType());
                    r.setStatus(job.getStatus());
                    r.setFavoritedAt(f.getCreatedAt());
                    return r;
                });
    }

    public boolean isFavorited(Long userId, Long jobId) {
        return favoriteRepository.existsByUserIdAndJobId(userId, jobId);
    }
}