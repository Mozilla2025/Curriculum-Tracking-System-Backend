package com.mozilla.curriculum_tracking_system.constants;

public final class CacheConstants {

    private CacheConstants() {
    }

    public static final String DEPARTMENTS = "departments";
    public static final String DEPARTMENT_BY_ID = "department_by_id";
    public static final String DEPARTMENTS_BY_SCHOOL = "departments_by_school";
    public static final String DEPARTMENTS_SEARCH = "departments_search";
    public static final String DEPARTMENTS_SEARCH_BY_SCHOOL = "departments_search_by_school";
    public static final String DEPARTMENT_COUNT_BY_SCHOOL = "department_count_by_school";
    public static final String DEPARTMENT_EXISTS = "department_exists";

    public static final String SCHOOLS = "schools";
    public static final String SCHOOL_BY_ID = "school_by_id";
    public static final String SCHOOL_EXISTS = "school_exists";

    public static final String DEPARTMENT_KEY_PREFIX = "dept";
    public static final String SCHOOL_KEY_PREFIX = "school";
    public static final String SEARCH_KEY_PREFIX = "search";
    public static final String COUNT_KEY_PREFIX = "count";
    public static final String EXISTS_KEY_PREFIX = "exists";
}