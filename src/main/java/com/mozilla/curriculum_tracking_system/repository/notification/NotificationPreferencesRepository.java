package com.mozilla.curriculum_tracking_system.repository.notification;

import com.mozilla.curriculum_tracking_system.model.notification.NotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, Long> {

    Optional<NotificationPreferences> findByUserId(Long userId);

    @Query("SELECT np FROM NotificationPreferences np WHERE np.weeklySummary = true")
    List<NotificationPreferences> findUsersWithWeeklySummaryEnabled();

    @Query("SELECT np FROM NotificationPreferences np WHERE np.overdueReminders = true")
    List<NotificationPreferences> findUsersWithOverdueRemindersEnabled();
}
