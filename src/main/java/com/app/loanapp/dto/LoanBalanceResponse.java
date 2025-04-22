package com.app.loanapp.dto;

import java.math.BigDecimal;

public class LoanBalanceResponse {
    private Long loanId;
    private BigDecimal remainingBalance;

    public LoanBalanceResponse(Long loanId, BigDecimal remainingBalance) {
        this.loanId = loanId;
        this.remainingBalance = remainingBalance;
    }

    // Getters and setters
    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }
}