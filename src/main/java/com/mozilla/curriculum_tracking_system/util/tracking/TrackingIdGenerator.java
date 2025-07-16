package com.mozilla.curriculum_tracking_system.util.tracking;

import org.springframework.stereotype.Component;

/**
 * Utility for generating unique tracking IDs
 */
@Component
public class TrackingIdGenerator {

    private static final String PREFIX = "TRK";
    private static final String SEPARATOR = "-";

    /**
     * Generate a unique tracking ID based on curriculum information
     */
    public String generateTrackingId(String curriculumCode, String departmentCode) {
        String cleanCurriculumCode = cleanCode(curriculumCode, "CUR");
        String cleanedDepartmentCode = cleanCode(departmentCode, "DEPT");
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);

        return PREFIX + SEPARATOR + cleanedDepartmentCode + SEPARATOR +
                cleanCurriculumCode + SEPARATOR + timestamp;
    }

    /**
     * Generate tracking ID for ideation stage (when curriculum doesn't exist yet)
     */
    public String generateIdeationTrackingId(String proposedCurriculumCode, String departmentCode, String schoolCode) {
        String cleanCurriculumCode = cleanCode(proposedCurriculumCode, "IDEA");
        String cleanDepartmentCode = cleanCode(departmentCode, "DEPT");
        String cleanSchoolCode = cleanCode(schoolCode, "SCH");
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);

        return PREFIX + SEPARATOR + cleanSchoolCode + SEPARATOR +
                cleanDepartmentCode + SEPARATOR + cleanCurriculumCode + SEPARATOR + timestamp;
    }

    private String cleanCode(String code, String fallback) {
        if (code == null || code.trim().isEmpty()) {
            return fallback;
        }
        String cleaned = code.replaceAll("[^A-Z0-9]", "").toUpperCase();
        return cleaned.length() > 6 ? cleaned.substring(0, 6) :
                cleaned.isEmpty() ? fallback : cleaned;
    }
}