package com.mozilla.curriculum_tracking_system.util;

import com.mozilla.curriculum_tracking_system.dto.curriculum.CurriculumSearchCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class CacheKeyGenerator {

    private static final String SEPARATOR = ":";
    private static final String NULL_VALUE = "null";

    /**
     * Generate a cache key by joining multiple parts with a separator
     *
     * @param parts The parts to join
     * @return The generated cache key
     */
    public String generateKey(String... parts) {
        return Arrays.stream(parts)
                .map(part -> part != null ? part : NULL_VALUE)
                .collect(Collectors.joining(SEPARATOR));
    }

    /**
     * Generate a cache key by joining multiple objects with a separator
     *
     * @param parts The objects to join (will be converted to strings)
     * @return The generated cache key
     */
    public String generateKey(Object... parts) {
        return Arrays.stream(parts)
                .map(part -> part != null ? part.toString() : NULL_VALUE)
                .collect(Collectors.joining(SEPARATOR));
    }

    /**
     * Generate a department-specific cache key with a type prefix
     *
     * @param type   The department operation type (e.g., "by_school", "search")
     * @param params Additional parameters for the key
     * @return The generated department cache key
     */
    public String generateDepartmentKey(String type, Object... params) {
        StringBuilder key = new StringBuilder("department").append(SEPARATOR).append(type);
        for (Object param : params) {
            key.append(SEPARATOR).append(param != null ? param.toString() : NULL_VALUE);
        }
        return key.toString();
    }

    /**
     * Generate a school-specific cache key with a type prefix
     *
     * @param type   The school operation type
     * @param params Additional parameters for the key
     * @return The generated school cache key
     */
    public String generateSchoolKey(String type, Object... params) {
        StringBuilder key = new StringBuilder("school").append(SEPARATOR).append(type);
        for (Object param : params) {
            key.append(SEPARATOR).append(param != null ? param.toString() : NULL_VALUE);
        }
        return key.toString();
    }

    /**
     * Generate a curriculum-specific cache key with a type prefix
     *
     * @param type   The curriculum operation type
     * @param params Additional parameters for the key
     * @return The generated curriculum cache key
     */
    public String generateCurriculumKey(String type, Object... params) {
        StringBuilder key = new StringBuilder("curriculum").append(SEPARATOR).append(type);
        for (Object param : params) {
            key.append(SEPARATOR).append(param != null ? param.toString() : NULL_VALUE);
        }
        return key.toString();
    }

    /**
     * Generate a pageable-aware cache key for department operations
     *
     * @param type     The operation type
     * @param pageable The pagination information
     * @param params   Additional parameters
     * @return The generated cache key with pagination info
     */
    public String generateDepartmentPageableKey(String type, Pageable pageable, Object... params) {
        StringBuilder key = new StringBuilder("department").append(SEPARATOR).append(type);

        // Add custom parameters first
        for (Object param : params) {
            key.append(SEPARATOR).append(param != null ? param.toString() : NULL_VALUE);
        }

        // Add pagination info
        key.append(SEPARATOR).append("page").append(SEPARATOR).append(pageable.getPageNumber())
                .append(SEPARATOR).append("size").append(SEPARATOR).append(pageable.getPageSize());

        // Add sort info if present
        if (pageable.getSort().isSorted()) {
            key.append(SEPARATOR).append("sort").append(SEPARATOR)
                    .append(pageable.getSort().toString().replaceAll("[^a-zA-Z0-9_-]", "_"));
        }

        return key.toString();
    }

    /**
     * Generate a pageable-aware cache key for curriculum operations
     *
     * @param type     The operation type
     * @param pageable The pagination information
     * @param params   Additional parameters
     * @return The generated cache key with pagination info
     */
    public String generateCurriculumPageableKey(String type, Pageable pageable, Object... params) {
        StringBuilder key = new StringBuilder("curriculum").append(SEPARATOR).append(type);

        // Add custom parameters first
        for (Object param : params) {
            key.append(SEPARATOR).append(param != null ? param.toString() : NULL_VALUE);
        }

        // Add pagination info
        key.append(SEPARATOR).append("page").append(SEPARATOR).append(pageable.getPageNumber())
                .append(SEPARATOR).append("size").append(SEPARATOR).append(pageable.getPageSize());

        // Add sort info if present
        if (pageable.getSort().isSorted()) {
            key.append(SEPARATOR).append("sort").append(SEPARATOR)
                    .append(pageable.getSort().toString().replaceAll("[^a-zA-Z0-9_-]", "_"));
        }

        return key.toString();
    }

    /**
     * Generate a search-specific cache key for departments
     *
     * @param searchTerm The search term
     * @param pageable   The pagination information
     * @param schoolId   Optional school ID for scoped search
     * @return The generated search cache key
     */
    public String generateDepartmentSearchKey(String searchTerm, Pageable pageable, Long schoolId) {
        StringBuilder key = new StringBuilder("department").append(SEPARATOR).append("search");

        // Add school ID if provided
        if (schoolId != null) {
            key.append(SEPARATOR).append("school").append(SEPARATOR).append(schoolId);
        }

        String normalizedSearchTerm = StringUtils.hasText(searchTerm) ?
                searchTerm.trim().toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "_") : "empty";
        key.append(SEPARATOR).append("term").append(SEPARATOR).append(normalizedSearchTerm);

        // Add pagination info
        key.append(SEPARATOR).append("page").append(SEPARATOR).append(pageable.getPageNumber())
                .append(SEPARATOR).append("size").append(SEPARATOR).append(pageable.getPageSize());

        // Add sort info if present
        if (pageable.getSort().isSorted()) {
            key.append(SEPARATOR).append("sort").append(SEPARATOR)
                    .append(pageable.getSort().toString().replaceAll("[^a-zA-Z0-9_-]", "_"));
        }

        return key.toString();
    }

    /**
     * Generate a search-specific cache key for curriculums
     *
     * @param criteria The search criteria
     * @param pageable The pagination information
     * @return The generated search cache key
     */
    public String generateCurriculumSearchKey(CurriculumSearchCriteria criteria, Pageable pageable) {
        StringBuilder key = new StringBuilder("curriculum").append(SEPARATOR).append("search");

        if (criteria.getSchoolId() != null) {
            key.append(SEPARATOR).append("school").append(SEPARATOR).append(criteria.getSchoolId());
        }

        if (criteria.getDepartmentId() != null) {
            key.append(SEPARATOR).append("dept").append(SEPARATOR).append(criteria.getDepartmentId());
        }

        if (criteria.getAcademicLevelId() != null) {
            key.append(SEPARATOR).append("level").append(SEPARATOR).append(criteria.getAcademicLevelId());
        }

        if (criteria.getStatus() != null) {
            key.append(SEPARATOR).append("status").append(SEPARATOR).append(criteria.getStatus());
        }

        if (StringUtils.hasText(criteria.getName())) {
            String normalizedSearchTerm = criteria.getName().trim().toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "_");
            key.append(SEPARATOR).append("name").append(SEPARATOR).append(normalizedSearchTerm);
        }

        if (StringUtils.hasText(criteria.getCode())) {
            String normalizedCode = criteria.getCode().trim().toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "_");
            key.append(SEPARATOR).append("code").append(SEPARATOR).append(normalizedCode);
        }

        key.append(SEPARATOR).append("page").append(SEPARATOR).append(pageable.getPageNumber())
                .append(SEPARATOR).append("size").append(SEPARATOR).append(pageable.getPageSize());

        if (pageable.getSort().isSorted()) {
            key.append(SEPARATOR).append("sort").append(SEPARATOR)
                    .append(pageable.getSort().toString().replaceAll("[^a-zA-Z0-9_-]", "_"));
        }

        return key.toString();
    }

    /**
     * Generate a cache key for curriculum expiring soon query
     *
     * @param days The number of days for expiring soon check
     * @return The generated cache key
     */
    public String generateCurriculumExpiringSoonKey(int days) {
        return generateCurriculumKey("expiring_soon", days);
    }

    /**
     * Generate a cache key for curriculum existence checks by name, department, and academic level
     *
     * @param name            The curriculum name
     * @param departmentId    The department ID
     * @param academicLevelId The academic level ID
     * @param excludeId       Optional ID to exclude (for updates)
     * @return The generated cache key
     */
    public String generateCurriculumExistsKey(String name, Long departmentId, Long academicLevelId, Long excludeId) {
        StringBuilder key = new StringBuilder("curriculum").append(SEPARATOR).append("exists");

        String normalizedName = StringUtils.hasText(name) ?
                name.trim().toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "_") : NULL_VALUE;

        key.append(SEPARATOR).append("name").append(SEPARATOR).append(normalizedName)
                .append(SEPARATOR).append("dept").append(SEPARATOR).append(departmentId)
                .append(SEPARATOR).append("level").append(SEPARATOR).append(academicLevelId);

        if (excludeId != null) {
            key.append(SEPARATOR).append("exclude").append(SEPARATOR).append(excludeId);
        }

        return key.toString();
    }

    /**
     * Generate a cache key for curriculum existence checks by code
     *
     * @param code      The curriculum code
     * @param excludeId Optional ID to exclude (for updates)
     * @return The generated cache key
     */
    public String generateCurriculumCodeExistsKey(String code, Long excludeId) {
        StringBuilder key = new StringBuilder("curriculum").append(SEPARATOR).append("exists").append(SEPARATOR).append("code");

        String normalizedCode = StringUtils.hasText(code) ?
                code.trim().toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "_") : NULL_VALUE;

        key.append(SEPARATOR).append(normalizedCode);

        if (excludeId != null) {
            key.append(SEPARATOR).append("exclude").append(SEPARATOR).append(excludeId);
        }

        return key.toString();
    }

    /**
     * Generate a compound key for operations involving multiple parameters
     *
     * @param prefix The key prefix
     * @param params The parameters to include in the key
     * @return The generated compound key
     */
    public String generateCompoundKey(String prefix, Object... params) {
        if (params == null || params.length == 0) {
            return prefix;
        }

        StringBuilder key = new StringBuilder(prefix);
        for (Object param : params) {
            key.append(SEPARATOR).append(param != null ? param.toString() : NULL_VALUE);
        }
        return key.toString();
    }

    /**
     * Generate a simple key for single parameter operations
     *
     * @param parameter The single parameter
     * @return The parameter as a string key
     */
    public String generateSimpleKey(Object parameter) {
        return parameter != null ? parameter.toString() : NULL_VALUE;
    }

    /**
     * Generate a count-specific cache key
     *
     * @param entity The entity type (e.g., "department", "school", "curriculum")
     * @param params Additional parameters for the count operation
     * @return The generated count cache key
     */
    public String generateCountKey(String entity, Object... params) {
        StringBuilder key = new StringBuilder(entity).append(SEPARATOR).append("count");
        for (Object param : params) {
            key.append(SEPARATOR).append(param != null ? param.toString() : NULL_VALUE);
        }
        return key.toString();
    }

    /**
     * Generate an exists-check cache key
     *
     * @param entity The entity type (e.g., "department", "school", "curriculum")
     * @param params Additional parameters for the exists check
     * @return The generated exists cache key
     */
    public String generateExistsKey(String entity, Object... params) {
        StringBuilder key = new StringBuilder(entity).append(SEPARATOR).append("exists");
        for (Object param : params) {
            key.append(SEPARATOR).append(param != null ? param.toString() : NULL_VALUE);
        }
        return key.toString();
    }
}