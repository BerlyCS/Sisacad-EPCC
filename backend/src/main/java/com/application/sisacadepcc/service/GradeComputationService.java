package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Grade;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GradeComputationService {

    private static final int EXPECTED_COMPONENTS = 3;
    private static final int SCALE = 2;

    public BigDecimal computeFinalGrade(Grade grade, Course course) {
        GradeWeightSnapshot weights = snapshotWeights(course);
        BigDecimal continuousContribution = weightedScore(grade.getContinuousGrades(), weights.continuousWeights());
        BigDecimal examContribution = weightedScore(grade.getExamGrades(), weights.examWeights());

        return continuousContribution.add(examContribution).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public GradeWeightSnapshot snapshotWeights(Course course) {
        List<BigDecimal> continuousWeights = sanitizeWeights(course != null ? course.getContinuousGradeWeights() : null);
        List<BigDecimal> examWeights = sanitizeWeights(course != null ? course.getExamGradeWeights() : null);
        normalizeWeights(continuousWeights, examWeights);
        return new GradeWeightSnapshot(List.copyOf(continuousWeights), List.copyOf(examWeights));
    }

    private List<BigDecimal> sanitizeWeights(List<BigDecimal> rawWeights) {
        List<BigDecimal> sanitized = new ArrayList<>(Collections.nCopies(EXPECTED_COMPONENTS, BigDecimal.ZERO));
        if (rawWeights == null || rawWeights.isEmpty()) {
            return sanitized;
        }

        for (int i = 0; i < EXPECTED_COMPONENTS && i < rawWeights.size(); i++) {
            BigDecimal value = rawWeights.get(i);
            sanitized.set(i, value != null ? value : BigDecimal.ZERO);
        }
        return sanitized;
    }

    private void normalizeWeights(List<BigDecimal> continuousWeights, List<BigDecimal> examWeights) {
        BigDecimal total = sum(continuousWeights).add(sum(examWeights));

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal equalWeight = BigDecimal.ONE
                    .divide(BigDecimal.valueOf(EXPECTED_COMPONENTS * 2L), SCALE + 2, RoundingMode.HALF_UP);
            for (int i = 0; i < EXPECTED_COMPONENTS; i++) {
                continuousWeights.set(i, equalWeight);
                examWeights.set(i, equalWeight);
            }
            total = equalWeight.multiply(BigDecimal.valueOf(EXPECTED_COMPONENTS * 2L));
        }

        BigDecimal normalizer = BigDecimal.ONE.divide(total, SCALE + 2, RoundingMode.HALF_UP);
        for (int i = 0; i < EXPECTED_COMPONENTS; i++) {
            continuousWeights.set(i, continuousWeights.get(i).multiply(normalizer));
            examWeights.set(i, examWeights.get(i).multiply(normalizer));
        }
    }

    private BigDecimal sum(List<BigDecimal> weights) {
        return weights.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal weightedScore(List<Integer> grades, List<BigDecimal> weights) {
        BigDecimal result = BigDecimal.ZERO;
        for (int i = 0; i < EXPECTED_COMPONENTS; i++) {
            BigDecimal gradeValue = BigDecimal.ZERO;
            if (grades != null && i < grades.size()) {
                Integer grade = grades.get(i);
                if (grade != null) {
                    gradeValue = BigDecimal.valueOf(grade);
                }
            }
            result = result.add(gradeValue.multiply(weights.get(i)));
        }
        return result;
    }
    public record GradeWeightSnapshot(List<BigDecimal> continuousWeights, List<BigDecimal> examWeights) {}
}
