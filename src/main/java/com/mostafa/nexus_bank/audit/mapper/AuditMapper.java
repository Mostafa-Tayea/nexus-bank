package com.mostafa.nexus_bank.audit.mapper;

import com.mostafa.nexus_bank.audit.dto.response.AuditResponse;
import com.mostafa.nexus_bank.audit.entity.Audit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "eventType", target = "eventType")
    @Mapping(source = "action", target = "action")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "ipAddress", target = "ipAddress")
    @Mapping(source = "device", target = "device")
    @Mapping(source = "httpMethod", target = "httpMethod")
    @Mapping(source = "endpoint", target = "endpoint")
    @Mapping(source = "requestId", target = "requestId")
    @Mapping(source = "referenceNumber", target = "referenceNumber")
    @Mapping(source = "timestamp", target = "timestamp")
    @Mapping(source = "result", target = "result")
    @Mapping(source = "details", target = "details")
    AuditResponse toResponse(Audit audit);
}
