package com.mozilla.curriculum_tracking_system.util;

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

        // Add search term (normalized)
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
     * @param entity The entity type (e.g., "department", "school")
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
     * @param entity The entity type (e.g., "department", "school")
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