package com.app.loanapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MarkRepaymentRequest {
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotBlank(message = "Payment method cannot be blank")
    @Pattern(regexp = "^(CASH|MPESA|BANK)$", message = "Payment method must be CASH, MPESA, or BANK")
    private String method;

    private String ref; // Required for MPESA and BANK, optional for CASH
}