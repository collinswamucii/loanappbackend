package com.app.loanapp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Customer customer;
    private double principalAmount;
    private double interestRate;
    private int repaymentPeriod;
    private String frequency; //weekly, monthly, yearly
    private Date startDate;
    private double totalPaid;
    private String status; //Active, Paid, Defaulted
}
