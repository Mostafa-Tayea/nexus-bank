package com.mostafa.nexus_bank.audit.entity;

import com.mostafa.nexus_bank.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audit extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, length = 255)
    private String action;

    @Column(length = 100)
    private String username;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(length = 255)
    private String device;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(length = 255)
    private String endpoint;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 20)
    private String result;

    @Column(length = 1000)
    private String details;
}
