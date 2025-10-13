package com.jobspring.user.client;

import com.jobspring.user.dto.PromoteToHrRequest;
import com.jobspring.user.dto.UserView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient("auth-service")
public interface AuthUserClient {

//    @GetMapping("/{userId}")
//    UserView getUser(@PathVariable("userId") Long userId);

    @PostMapping(value = "/{userId}/make-hr",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> promoteToHr(@PathVariable("userId") Long userId,
                                     @RequestBody(required = false) PromoteToHrRequest req);
}
