package com.jobspring.user.client;

import com.jobspring.user.dto.PageResponse;
import com.jobspring.user.dto.PromoteToHrRequest;
import com.jobspring.user.dto.UserDTO;
import com.jobspring.user.dto.UserView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("auth-service")
public interface AuthUserClient {

//    @GetMapping("/{userId}")
//    UserView getUser(@PathVariable("userId") Long userId);

    @PostMapping(value = "/{userId}/make-hr",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> promoteToHr(@PathVariable("userId") Long userId,
                                     @RequestBody(required = false) PromoteToHrRequest req);

    @GetMapping("/search")
    PageResponse<UserDTO> search(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "sort", required = false) List<String> sort
    );

    @PostMapping("/briefs:batch")
    List<AccountBrief> batchAccountBriefs(@RequestBody List<Long> userIds);


    record AccountBrief(Long id, String fullName) {
    }

    @GetMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    UserView getCurrentUser(@RequestHeader("X-User-Id") String uid);

    @GetMapping("/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}
