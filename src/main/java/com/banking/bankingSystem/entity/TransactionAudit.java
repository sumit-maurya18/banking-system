
package com.banking.bankingSystem.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "transaction_audit")
public class TransactionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_record_id", nullable = false)
    private Transaction transaction;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "thread_name", length = 100)
    private String threadName;

    @Column(name = "operation", nullable = false, length = 100)
    private String operation;

    @Column(name = "result", nullable = false, length = 20)
    private String result;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "message", length = 255)
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Required by JPA
    public TransactionAudit() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    // Getters and Setters

    public Long getAuditId() {
        return auditId;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}