package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.valueobject.Content;
import com.application.sisacadepcc.service.exception.InvalidSyllabusException;
import com.application.sisacadepcc.service.exception.SyllabusStorageException;
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
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SyllabusStorageService {

    private static final long MAX_SIZE_BYTES = 100L * 1024L * 1024L; // 100 MB
    private static final Logger LOGGER = LoggerFactory.getLogger(SyllabusStorageService.class);

    private final Path storageDirectory;
    public SyllabusStorageService(@Value("${app.syllabus.storage-location:storage/syllabus}") String storageLocation) {
        this.storageDirectory = Paths.get(storageLocation).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException e) {
            throw new SyllabusStorageException("Unable to initialize syllabus storage", e);
        }
    }

    public Content storeFile(MultipartFile file, Long courseId) {
        if (file == null || file.isEmpty()) {
            throw new InvalidSyllabusException("The uploaded file is empty");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidSyllabusException("The syllabus file exceeds the 100 MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new InvalidSyllabusException("Only PDF syllabus files are accepted");
        }

        String generatedName = buildStoredFileName(courseId);
        Path targetFile = resolvePath(generatedName);

        try {
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            return new Content(generatedName, contentType, generatedName, file.getSize());
        } catch (IOException e) {
            throw new SyllabusStorageException("Could not store the syllabus file", e);
        }
    }

    public Resource loadAsResource(Content content) {
        if (content == null || content.getUrl() == null) {
            throw new SyllabusStorageException("No syllabus file is associated with this record");
        }

        Path filePath = resolvePath(content.getUrl());
        try {
            URI fileUri = Objects.requireNonNull(filePath).toUri();
            Resource resource = new UrlResource(Objects.requireNonNull(fileUri));
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (MalformedURLException e) {
            throw new SyllabusStorageException("Invalid syllabus file path", e);
        }
        throw new SyllabusStorageException("Syllabus file not found on disk");
    }

    public void deleteFile(Content content) {
        if (content == null || content.getUrl() == null) {
            return;
        }
        Path filePath = resolvePath(content.getUrl());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            LOGGER.warn("Failed to delete syllabus file {}", filePath, e);
        }
    }

    private String buildStoredFileName(Long courseId) {
        return "course-" + courseId + "_syllabus.pdf";
    }

    private Path resolvePath(String relativePath) {
        return storageDirectory.resolve(relativePath).normalize();
    }
}
