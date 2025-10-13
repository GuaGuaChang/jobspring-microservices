package com.jobspring.user.controller;

import com.jobspring.user.dto.PromoteToHrRequest;
import com.jobspring.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{userId}/make-hr")
    public ResponseEntity<Void> makeHr(@PathVariable("userId") Long userId,
                                       @RequestBody(required = false) PromoteToHrRequest req) {
        userService.makeHr(userId, req);
        return ResponseEntity.noContent().build();
    }

}
