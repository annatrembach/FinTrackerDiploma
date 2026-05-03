package com.tracker.TransactionService.models;

import com.tracker.TransactionService.models.enums.Recurrence;
import com.tracker.TransactionService.models.enums.ScheduledStatus;
import com.tracker.TransactionService.models.enums.Type;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
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

}