package com.jobspring.user.api;

import com.jobspring.user.dto.FavoriteJobResponse;
import com.jobspring.user.service.JobFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/job_favorites")
@RequiredArgsConstructor
public class JobFavoriteController {

    private final JobFavoriteService favoriteService;

    // 收藏
    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping("/{jobId}")
    public ResponseEntity<Void> add(@PathVariable Long jobId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        favoriteService.add(userId, jobId);
        return ResponseEntity.noContent().build();
    }

    // 取消收藏
    @PreAuthorize("hasRole('CANDIDATE')")
    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> remove(@PathVariable Long jobId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        favoriteService.remove(userId, jobId);
        return ResponseEntity.noContent().build();
    }

    // 我的收藏列表
    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping
    public ResponseEntity<Page<FavoriteJobResponse>> myFavorites(Authentication auth, Pageable pageable) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(favoriteService.list(userId, pageable));
    }

    // 是否已收藏
    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/{jobId}/is-favorited")
    public ResponseEntity<Boolean> isFavorited(@PathVariable Long jobId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(favoriteService.isFavorited(userId, jobId));
    }


}