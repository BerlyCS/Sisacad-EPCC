package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.domain.model.Secretary;
import com.application.sisacadepcc.service.SecretaryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secretaries")
public class SecretaryController {

    private final SecretaryService service;

    public SecretaryController(SecretaryService service) {
        this.service = service;
    }

    @GetMapping
    public List<Secretary> getAllSecretaries() {
        return service.getAllSecretaries();
    }
}
