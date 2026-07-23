package com.mostafa.nexus_bank.notification.mapper;

import com.mostafa.nexus_bank.notification.dto.response.NotificationResponse;
import com.mostafa.nexus_bank.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "message", target = "message")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "sentAt", target = "sentAt")
    @Mapping(source = "createdAt", target = "createdAt")
    NotificationResponse toResponse(Notification notification);
}
