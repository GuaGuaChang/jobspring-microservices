package com.jobspring.notification.api;

import com.jobspring.notification.dto.SendCodeRequestDTO;
import com.jobspring.notification.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final VerificationService verificationService;

    //供auth-service调用
    @PostMapping("/send-code")
    public ResponseEntity<Void> sendCode(@Valid @RequestBody SendCodeRequestDTO req) {
        verificationService.sendRegisterCode(req.getEmail());
        return ResponseEntity.noContent().build();
    }
}
