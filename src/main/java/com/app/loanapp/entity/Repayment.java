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
    private double amount;
    private Date dueDate;
    private String status; //Pending, Paid
}
