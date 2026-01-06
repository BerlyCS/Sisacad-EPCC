package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.domain.model.Secretary;
import com.application.sisacadepcc.service.SecretaryService;
import com.application.sisacadepcc.service.dto.UserImportResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/secretaries")
public class SecretaryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecretaryController.class);

    private final SecretaryService service;

    public SecretaryController(SecretaryService service) {
        this.service = service;
    }

    @GetMapping
    @RequiresAdministratorAccess
    public ResponseEntity<List<Secretary>> getAllSecretaries() {
        return ResponseEntity.ok(service.getAllSecretaries());
    }

    @PostMapping
    @RequiresAdministratorAccess
    public ResponseEntity<?> createSecretary(@RequestBody Secretary secretary) {
        try {
            Secretary created = service.createSecretary(secretary);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            LOGGER.error("Error registrando secretaria", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "No se pudo registrar la secretaria"));
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequiresAdministratorAccess
    public ResponseEntity<UserImportResult<Secretary>> importSecretaries(@RequestPart("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(service.importSecretaries(file));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new UserImportResult<>(List.of(), List.of(ex.getMessage()), 0, 0));
        }
    }
}
