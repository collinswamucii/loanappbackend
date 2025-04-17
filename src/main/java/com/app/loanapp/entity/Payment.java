package com.app.loanapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String paymentRef;
    @NotBlank(message = "Payment method cannot be blank")
    private String paymentMethod; // CASH, MPESA, BANK
    private double amount;
    private Date datePaid;
    @ManyToOne
    private Loan loan;
    @ManyToOne
    private Repayment repayment; // Link to the specific repayment installment
}