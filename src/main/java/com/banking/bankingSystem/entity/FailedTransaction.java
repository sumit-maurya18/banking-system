package com.banking.bankingSystem.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "failed_transactions")
public class FailedTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "failure_id")
    private Long failureId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "transaction_record_id",
            nullable = false,
            unique = true
    )
    private Transaction transaction;

    @Column(name = "error_category", nullable = false, length = 50)
    private String errorCategory;

    @Column(name = "failure_reason", nullable = false, length = 255)
    private String failureReason;

    @Column(name = "failure_stage", nullable = false, length = 50)
    private String failureStage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Required by JPA
    public FailedTransaction() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    // Getters and Setters

    public Long getFailureId() {
        return failureId;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getErrorCategory() {
        return errorCategory;
    }

    public void setErrorCategory(String errorCategory) {
        this.errorCategory = errorCategory;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getFailureStage() {
        return failureStage;
    }

    public void setFailureStage(String failureStage) {
        this.failureStage = failureStage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}