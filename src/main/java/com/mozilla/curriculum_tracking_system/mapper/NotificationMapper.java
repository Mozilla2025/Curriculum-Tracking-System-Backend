package com.mozilla.curriculum_tracking_system.mapper;

import com.mozilla.curriculum_tracking_system.dto.notification.NotificationDto;
import com.mozilla.curriculum_tracking_system.model.notification.Notification;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final ModelMapper modelMapper;

    public NotificationDto toDto(Notification notification) {
        NotificationDto dto = modelMapper.map(notification, NotificationDto.class);

        // Map user details
        if (notification.getUser() != null) {
            dto.setUserId(notification.getUser().getId());
            dto.setEmail(notification.getUser().getEmail());
            dto.setUsername(notification.getUser().getUsername());
        }

        // Map curriculum details
        if (notification.getCurriculum() != null) {
            dto.setCurriculumId(notification.getCurriculum().getId());
            dto.setCurriculumName(notification.getCurriculum().getName());
        }

        return dto;
    }

    public Notification toEntity(NotificationDto dto, User user, Curriculum curriculum) {
        Notification notification = modelMapper.map(dto, Notification.class);

        notification.setUser(user);
        notification.setCurriculum(curriculum);

        return notification;
    }

    public void updateEntityFromDto(NotificationDto dto, Notification notification) {
        modelMapper.map(dto, notification);
        // Note: User and Curriculum relationships should be set separately
    }
}