package com.tracker.TransactionService.controllers;

import com.tracker.TransactionService.models.ScheduledTransaction;
import com.tracker.TransactionService.models.DTO.UserDTO;
import com.tracker.TransactionService.services.ScheduledTransactionService;
import com.tracker.TransactionService.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduled-transactions")
@CrossOrigin(origins = "http://localhost:3000/", allowCredentials = "true")
public class ScheduledTransactionController {

    @Autowired
    private ScheduledTransactionService scheduledService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<ScheduledTransaction> create(
            @RequestBody ScheduledTransaction scheduled,
            @RequestHeader("Authorization") String jwt) throws Exception {

        UserDTO user = userService.getUserProfile(jwt);
        scheduled.setUserId(user.getId());

        return new ResponseEntity<>(scheduledService.create(scheduled), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ScheduledTransaction> update(
            @PathVariable Long id,
            @RequestBody ScheduledTransaction updated) throws Exception {
        return ResponseEntity.ok(scheduledService.update(id, updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduledTransaction> getById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(scheduledService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ScheduledTransaction>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(scheduledService.getByUserId(userId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ScheduledTransaction> cancel(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(scheduledService.cancel(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
        scheduledService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
