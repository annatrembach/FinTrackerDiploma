package com.tracker.TransactionService.models;

import com.tracker.TransactionService.models.enums.Recurrence;
import com.tracker.TransactionService.models.enums.ScheduledStatus;
import com.tracker.TransactionService.models.enums.Type;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_transaction")
public class ScheduledTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private Type type;

    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoryId")
    private Category category;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "scheduledDate")
    private LocalDateTime scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence")
    private Recurrence recurrence;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ScheduledStatus status;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "executedTransactionId")
    private Long executedTransactionId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Recurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }

    public ScheduledStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduledStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getExecutedTransactionId() {
        return executedTransactionId;
    }

    public void setExecutedTransactionId(Long executedTransactionId) {
        this.executedTransactionId = executedTransactionId;
    }
}