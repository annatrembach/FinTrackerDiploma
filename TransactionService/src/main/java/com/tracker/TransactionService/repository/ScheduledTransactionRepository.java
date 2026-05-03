package com.tracker.TransactionService.repository;

import com.tracker.TransactionService.models.ScheduledTransaction;
import com.tracker.TransactionService.models.enums.ScheduledStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransaction, Long> {

    List<ScheduledTransaction> findByUserId(Long userId);

    List<ScheduledTransaction> findByStatusAndScheduledDateBefore(
            ScheduledStatus status,
            LocalDateTime now
    );
}