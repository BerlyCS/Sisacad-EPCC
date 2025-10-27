package com.application.sisacadepcc.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.domain.repository.SyllabusRepository;

@Service
public class SyllabusService {

    private final SyllabusRepository syllabusRepository;

    public SyllabusService(SyllabusRepository syllabusRepository) {
        this.syllabusRepository = syllabusRepository;
    }

    public List<Syllabus> getAllSyllabus() {
        return syllabusRepository.findAll();
    }

}