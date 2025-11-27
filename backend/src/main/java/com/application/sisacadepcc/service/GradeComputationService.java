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
    private static final int WEIGHT_SCALE = 6;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal MAX_GRADE = BigDecimal.valueOf(20);

    public BigDecimal computeFinalGrade(Grade grade, Course course) {
        GradeWeightSnapshot weights = snapshotWeights(course);
        BigDecimal continuousContribution = weightedScore(grade.getContinuousGrades(), weights.continuousWeights());
        BigDecimal examContribution = weightedScore(grade.getExamGrades(), weights.examWeights());
        BigDecimal finalGrade = continuousContribution.add(examContribution);
        if (finalGrade.compareTo(MAX_GRADE) > 0) {
            finalGrade = MAX_GRADE;
        }

        BigDecimal normalized = finalGrade.setScale(SCALE, RoundingMode.HALF_UP);

        // Custom rounding rule: round up to next integer only if fractional part >= 0.45
        BigDecimal integerPart = normalized.setScale(0, RoundingMode.DOWN);
        BigDecimal fractional = normalized.subtract(integerPart);
        BigDecimal threshold = BigDecimal.valueOf(0.45);
        if (fractional.compareTo(threshold) >= 0) {
            return integerPart.add(BigDecimal.ONE);
        }
        return integerPart;
    }

    public GradeWeightSnapshot snapshotWeights(Course course) {
        List<Integer> continuousRaw = course != null ? course.getContinuousGradeWeights() : null;
        List<Integer> examRaw = course != null ? course.getExamGradeWeights() : null;
        List<BigDecimal> continuousWeights = sanitizeWeights(continuousRaw);
        List<BigDecimal> examWeights = sanitizeWeights(examRaw);
        normalizeWeights(continuousWeights, examWeights);
        return new GradeWeightSnapshot(List.copyOf(continuousWeights), List.copyOf(examWeights));
    }

    private List<BigDecimal> sanitizeWeights(List<Integer> rawWeights) {
        List<BigDecimal> sanitized = new ArrayList<>(Collections.nCopies(EXPECTED_COMPONENTS, BigDecimal.ZERO));
        if (rawWeights == null || rawWeights.isEmpty()) {
            return sanitized;
        }

        for (int i = 0; i < EXPECTED_COMPONENTS && i < rawWeights.size(); i++) {
            Integer value = rawWeights.get(i);
            sanitized.set(i, value != null && value > 0 ? BigDecimal.valueOf(value) : BigDecimal.ZERO);
        }
        return sanitized;
    }

    private void normalizeWeights(List<BigDecimal> continuousWeights, List<BigDecimal> examWeights) {
        BigDecimal total = sum(continuousWeights).add(sum(examWeights));

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal equalShare = ONE_HUNDRED
                    .divide(BigDecimal.valueOf(EXPECTED_COMPONENTS * 2L), WEIGHT_SCALE, RoundingMode.HALF_UP);
            for (int i = 0; i < EXPECTED_COMPONENTS; i++) {
                continuousWeights.set(i, equalShare);
                examWeights.set(i, equalShare);
            }
            adjustRemainder(continuousWeights, examWeights);
            return;
        }

        if (total.compareTo(ONE_HUNDRED) != 0) {
            BigDecimal scalingFactor = ONE_HUNDRED.divide(total, WEIGHT_SCALE, RoundingMode.HALF_UP);
            for (int i = 0; i < EXPECTED_COMPONENTS; i++) {
                continuousWeights.set(i, continuousWeights.get(i)
                        .multiply(scalingFactor)
                        .setScale(WEIGHT_SCALE, RoundingMode.HALF_UP));
                examWeights.set(i, examWeights.get(i)
                        .multiply(scalingFactor)
                        .setScale(WEIGHT_SCALE, RoundingMode.HALF_UP));
            }
        }

        adjustRemainder(continuousWeights, examWeights);
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
            BigDecimal weightPercent = (weights != null && i < weights.size() && weights.get(i) != null)
                    ? weights.get(i).divide(ONE_HUNDRED, WEIGHT_SCALE, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            result = result.add(gradeValue.multiply(weightPercent));
        }
        return result;
    }

    private void adjustRemainder(List<BigDecimal> continuousWeights, List<BigDecimal> examWeights) {
        BigDecimal combined = sum(continuousWeights).add(sum(examWeights));
        BigDecimal delta = ONE_HUNDRED.subtract(combined).setScale(WEIGHT_SCALE, RoundingMode.HALF_UP);
        if (delta.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        if (!applyDelta(examWeights, delta)) {
            applyDelta(continuousWeights, delta);
        }
    }

    private boolean applyDelta(List<BigDecimal> weights, BigDecimal delta) {
        for (int i = EXPECTED_COMPONENTS - 1; i >= 0; i--) {
            BigDecimal current = weights.get(i);
            if (current != null) {
                weights.set(i, current.add(delta).setScale(WEIGHT_SCALE, RoundingMode.HALF_UP));
                return true;
            }
        }
        return false;
    }

    public record GradeWeightSnapshot(List<BigDecimal> continuousWeights, List<BigDecimal> examWeights) {}
}
