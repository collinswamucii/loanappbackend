package com.app.loanapp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Repayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Loan loan;
    private double principalAmount; // Principal portion of the payment
    private double interestAmount; // Interest portion of the payment
    private double amount; // Total amount (principal + interest)
    private Date dueDate;
    private String status;
}