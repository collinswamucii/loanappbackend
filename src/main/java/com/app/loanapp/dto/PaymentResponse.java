package com.app.loanapp.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PaymentResponse {
    private Long repaymentId;
    private double amount;
    private String method;
    private String ref;
    private Date date;
}