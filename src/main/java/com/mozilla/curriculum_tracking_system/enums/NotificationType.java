package com.mozilla.curriculum_tracking_system.enums;

public enum NotificationType {
    CURRICULUM_DUE_FOR_REVIEW,
    CURRICULUM_DELAY_REMINDER,
    CURRICULUM_OVERDUE,
    REVIEW_SUBMITTED,
    REVIEW_APPROVED,
    REVIEW_REJECTED,
    REVIEW_NEEDS_REVISION, //should it be major and minor revamp
    REVIEW_OVERDUE,

    WEEKLY_SUMMARY,
    BULK_NOTIFICATION,
    SYSTEM_NOTIFICATION
}