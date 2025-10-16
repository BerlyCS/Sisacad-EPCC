package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.domain.model.Administrator;
import com.application.sisacadepcc.service.AdministratorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/administrators")
public class AdministratorController {
    private final AdministratorService service;

    public AdministratorController(AdministratorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Administrator> getAllAdministrators() {
        return service.getAllAdministators();
    }
}
