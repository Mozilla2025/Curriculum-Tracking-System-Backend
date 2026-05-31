package com.mozilla.curriculum_tracking_system.constants;

public final class CacheConstants {

    // Department cache constants
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
    public static final String CURRICULUMS = "curriculums";
    public static final String CURRICULUM_BY_ID = "curriculum_by_id";
    public static final String CURRICULUMS_BY_SCHOOL = "curriculums_by_school";
    public static final String CURRICULUMS_BY_DEPARTMENT = "curriculums_by_department";
    public static final String CURRICULUMS_BY_ACADEMIC_LEVEL = "curriculums_by_academic_level";
    public static final String CURRICULUMS_SEARCH = "curriculums_search";
    public static final String CURRICULUMS_EXPIRING_SOON = "curriculums_expiring_soon";
    public static final String CURRICULUM_STATS = "curriculum_stats";
    public static final String CURRICULUM_EXISTS_BY_NAME_DEPT_LEVEL = "curriculum_exists_by_name_dept_level";
    public static final String CURRICULUM_EXISTS_BY_CODE = "curriculum_exists_by_code";
    public static final String CURRICULUM_PREFIX = "curriculum";
    public static final String DEPARTMENT_PREFIX = "department";

    private CacheConstants() {
    }
}