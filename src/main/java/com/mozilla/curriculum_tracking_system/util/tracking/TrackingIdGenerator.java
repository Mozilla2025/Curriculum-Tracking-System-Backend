package com.mozilla.curriculum_tracking_system.util.tracking;

import org.springframework.stereotype.Component;

/**
 * utility for generating unique tracking IDs
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
     * Generate a tracking ID with custom components
     */
    public String generateTrackingId(String... components) {
        StringBuilder builder = new StringBuilder(PREFIX);

        for (String component : components) {
            if (component != null && !component.trim().isEmpty()) {
                builder.append(SEPARATOR).append(cleanCode(component, "X"));
            }
        }

        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        builder.append(SEPARATOR).append(timestamp);

        return builder.toString();
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
