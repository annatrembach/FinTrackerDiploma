package com.tracker.TransactionService.services.impls;

import com.tracker.TransactionService.models.ScheduledTransaction;
import com.tracker.TransactionService.models.Transaction;
import com.tracker.TransactionService.models.enums.Recurrence;
import com.tracker.TransactionService.models.enums.ScheduledStatus;
import com.tracker.TransactionService.publisher.TransactionProducer;
import com.tracker.TransactionService.repository.ScheduledTransactionRepository;
import com.tracker.TransactionService.repository.TransactionRepository;
import com.tracker.TransactionService.services.ScheduledTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledTransactionServiceImpl implements ScheduledTransactionService {

    private final ScheduledTransactionRepository scheduledRepo;
    private final TransactionRepository transactionRepository;
    private final TransactionProducer transactionProducer;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public ScheduledTransaction create(ScheduledTransaction scheduled) {
        scheduled.setStatus(ScheduledStatus.PENDING);
        scheduled.setCreatedAt(LocalDateTime.now());
        return scheduledRepo.save(scheduled);
    }

    @Override
    public ScheduledTransaction getById(Long id) throws Exception {
        return scheduledRepo.findById(id)
                .orElseThrow(() -> new Exception("ScheduledTransaction not found: " + id));
    }

    @Override
    public List<ScheduledTransaction> getByUserId(Long userId) {
        return scheduledRepo.findByUserId(userId);
    }

    @Override
    public ScheduledTransaction cancel(Long id) throws Exception {
        ScheduledTransaction st = getById(id);
        st.setStatus(ScheduledStatus.CANCELLED);
        return scheduledRepo.save(st);
    }

    /**
     * Кожну хвилину перевіряємо чи є PENDING транзакції
     * scheduledDate яких вже минула.
     */
    @Override
    @Scheduled(fixedDelay = 60_000)
    public void executeDue() {
        List<ScheduledTransaction> due = scheduledRepo
                .findByStatusAndScheduledDateBefore(ScheduledStatus.PENDING, LocalDateTime.now());

        for (ScheduledTransaction st : due) {
            try {
                // 1. Створити реальну транзакцію
                Transaction tx = buildTransaction(st);
                tx = transactionRepository.save(tx);
                transactionProducer.sendTransactionUpdate(tx);

                // 2. Оновити scheduled запис
                st.setExecutedTransactionId(tx.getId());

                if (st.getRecurrence() == Recurrence.ONCE) {
                    st.setStatus(ScheduledStatus.EXECUTED);
                } else {
                    // Для recurring — переносимо дату і залишаємо PENDING
                    st.setScheduledDate(nextDate(st));
                }

                scheduledRepo.save(st);

            } catch (Exception e) {
                // Логуємо але не зупиняємо обробку інших
                System.err.println("Failed to execute scheduled tx " + st.getId() + ": " + e.getMessage());
            }
        }
    }

    // ---------- helpers ----------

    private Transaction buildTransaction(ScheduledTransaction st) {
        Transaction tx = new Transaction();
        tx.setAmount(st.getAmount());
        tx.setType(st.getType());
        tx.setCategory(st.getCategory());
        tx.setDescription(st.getDescription());
        tx.setUserId(st.getUserId());
        tx.setTransactionDate(st.getScheduledDate().format(DATE_FMT));
        tx.setCreationDate(LocalDateTime.now());
        return tx;
    }

    private LocalDateTime nextDate(ScheduledTransaction st) {
        return switch (st.getRecurrence()) {
            case DAILY   -> st.getScheduledDate().plusDays(1);
            case WEEKLY  -> st.getScheduledDate().plusWeeks(1);
            case MONTHLY -> st.getScheduledDate().plusMonths(1);
            default      -> st.getScheduledDate(); // ONCE — не повинно сюди потрапляти
        };
    }

    @Override
    public ScheduledTransaction update(Long id, ScheduledTransaction updated) throws Exception {
        ScheduledTransaction existing = getById(id);

        if (updated.getAmount() != null)
            existing.setAmount(updated.getAmount());

        if (updated.getType() != null)
            existing.setType(updated.getType());

        if (updated.getCategory() != null)
            existing.setCategory(updated.getCategory());

        if (updated.getDescription() != null)
            existing.setDescription(updated.getDescription());

        if (updated.getScheduledDate() != null)
            existing.setScheduledDate(updated.getScheduledDate());

        if (updated.getRecurrence() != null)
            existing.setRecurrence(updated.getRecurrence());

        return scheduledRepo.save(existing);
    }
}