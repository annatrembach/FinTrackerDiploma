package com.tracker.TransactionService.models;

import com.tracker.TransactionService.models.enums.Type;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
public class Transaction  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Type type;

    private String description;

    @Column(name = "transactionDate")
    private String transactionDate;

    @Column(name = "createdAt")
    private LocalDateTime creationDate;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "goalId")
    private Long goalId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoryId")
    private Category category;

}
