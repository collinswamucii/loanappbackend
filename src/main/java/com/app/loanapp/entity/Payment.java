package com.app.loanapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String paymentRef;
    private double amount;
    private Date datePaid;
    @ManyToOne
    private Loan loan;
}