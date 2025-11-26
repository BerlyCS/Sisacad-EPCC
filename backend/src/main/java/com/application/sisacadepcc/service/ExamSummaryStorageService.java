package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.valueobject.Content;
import com.application.sisacadepcc.domain.model.valueobject.ExamStatisticType;
import com.application.sisacadepcc.service.exception.ExamSummaryStorageException;
import com.application.sisacadepcc.service.exception.ExamSummaryValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Service
public class ExamSummaryStorageService {

    private static final long MAX_SIZE_BYTES = 50L * 1024L * 1024L; // 50 MB
    private static final Logger LOGGER = LoggerFactory.getLogger(ExamSummaryStorageService.class);

    private final Path storageDirectory;

    public ExamSummaryStorageService(@Value("${app.exam-summary.storage-location:storage/exam-summaries}") String storageLocation) {
        this.storageDirectory = Paths.get(storageLocation).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException e) {
            throw new ExamSummaryStorageException("Unable to initialize exam summary storage", e);
        }
    }

    public Content storeFile(MultipartFile file,
                             Long groupId,
                             int examNumber,
                             ExamStatisticType summaryType) {
        if (file == null || file.isEmpty()) {
            throw new ExamSummaryValidationException("The uploaded PDF cannot be empty");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ExamSummaryValidationException("The PDF exceeds the 50 MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new ExamSummaryValidationException("Only PDF files are accepted for exam summaries");
        }

        String generatedName = buildStoredFileName(groupId, examNumber, summaryType);
        Path targetFile = resolvePath(generatedName);

        try {
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            return new Content(generatedName, contentType, generatedName, file.getSize());
        } catch (IOException e) {
            throw new ExamSummaryStorageException("Could not store the exam summary", e);
        }
    }

    public Resource loadAsResource(Content content) {
        if (content == null || content.getUrl() == null) {
            throw new ExamSummaryStorageException("No stored PDF was found for this summary");
        }

        Path filePath = resolvePath(content.getUrl());
        try {
            URI fileUri = Objects.requireNonNull(filePath).toUri();
            Resource resource = new UrlResource(Objects.requireNonNull(fileUri));
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (MalformedURLException e) {
            throw new ExamSummaryStorageException("Invalid exam summary path", e);
        }
        throw new ExamSummaryStorageException("Exam summary PDF not found on disk");
    }

    public void deleteFile(Content content) {
        if (content == null || content.getUrl() == null) {
            return;
        }
        Path filePath = resolvePath(content.getUrl());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            LOGGER.warn("Failed to delete exam summary file {}", filePath, e);
        }
    }

    private String buildStoredFileName(Long groupId, int examNumber, ExamStatisticType summaryType) {
        String safeType = summaryType.name().toLowerCase(Locale.ROOT);
        long timestamp = Instant.now().toEpochMilli();
        return "group-" + groupId + "_exam" + examNumber + "_" + safeType + "_" + timestamp + ".pdf";
    }

    private Path resolvePath(String relativePath) {
        return storageDirectory.resolve(relativePath).normalize();
    }
}
