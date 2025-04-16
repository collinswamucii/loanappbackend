package com.app.loanapp.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PaymentMessage {
    private String paymentRef;
    private double amount;
    private String phoneNumber;
    private Date datePaid;
}
