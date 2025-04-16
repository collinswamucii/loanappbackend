package com.app.loanapp.controller;

import com.app.loanapp.entity.Payment;
import com.app.loanapp.exception.NotFoundException;
import com.app.loanapp.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<Payment>> getPaymentsByLoan(@PathVariable Long loanId) {
        List<Payment> payments = paymentRepository.findAll().stream()
                .filter(payment -> payment.getLoan().getId().equals(loanId))
                .toList();
        if (payments.isEmpty()) {
            throw new NotFoundException("No payments found for loan with ID: " + loanId);
        }
        return ResponseEntity.ok(payments);
    }
}