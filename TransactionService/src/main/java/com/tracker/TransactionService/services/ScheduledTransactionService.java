package com.tracker.TransactionService.services;

import com.tracker.TransactionService.models.ScheduledTransaction;
import java.util.List;

public interface ScheduledTransactionService {

    ScheduledTransaction create(ScheduledTransaction scheduled);

    ScheduledTransaction getById(Long id) throws Exception;

    List<ScheduledTransaction> getByUserId(Long userId);

    ScheduledTransaction cancel(Long id) throws Exception;

    void executeDue();

    ScheduledTransaction update(Long id, ScheduledTransaction updated) throws Exception;

    void delete(Long id) throws Exception;
}