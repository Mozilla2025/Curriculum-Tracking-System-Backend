package com.mozilla.curriculum_tracking_system.repository.notification;

import com.mozilla.curriculum_tracking_system.model.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
