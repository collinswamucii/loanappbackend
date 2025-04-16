package com.app.loanapp.controller;

import com.app.loanapp.dto.PaymentMessage;
import com.app.loanapp.entity.Loan;
import com.app.loanapp.repository.CustomerRepository;
import com.app.loanapp.repository.LoanRepository;
import com.app.loanapp.service.PaymentService;
import com.app.loanapp.util.PaymentMessageParser;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoanController {
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/loans")
    public ResponseEntity<Loan> createLoan(@Valid @RequestBody Loan loan) {
        return ResponseEntity.ok(
                customerRepository.findById(loan.getCustomer().getId())
                        .map(customer -> {
                            loan.setCustomer(customer);
                            loan.setStartDate(new Date());
                            loan.setStatus("ACTIVE");
                            loan.setTotalPaid(0.0);
                            return loanRepository.save(loan);
                        })
                        .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + loan.getCustomer().getId()))
        );
    }

    @GetMapping("/loans/customer/{customerId}")
    public ResponseEntity<List<Loan>> getLoansByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(loanRepository.findByCustomerId(customerId));
    }

    @PostMapping("/payments")
    public ResponseEntity<Map<String, Object>> recordPayment(@RequestBody Map<String, String> paymentRequest) {
        String message = paymentRequest.get("message");
        if (message == null) {
            throw new IllegalArgumentException("Payment message is required");
        }

        // Parse the payment message
        PaymentMessage paymentMessage = PaymentMessageParser.parse(message);

        // Process the payment
        Loan updatedLoan = paymentService.recordPayment(
                paymentMessage.getPhoneNumber(),
                paymentMessage
        );

        // Return a detailed response
        return ResponseEntity.ok(Map.of(
                "paymentRef", paymentMessage.getPaymentRef(),
                "amount", paymentMessage.getAmount(),
                "phoneNumber", paymentMessage.getPhoneNumber(),
                "datePaid", paymentMessage.getDatePaid(),
                "loanId", updatedLoan.getId(),
                "totalPaid", updatedLoan.getTotalPaid(),
                "status", updatedLoan.getStatus()
        ));
    }
}