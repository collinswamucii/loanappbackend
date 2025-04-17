package com.app.loanapp.controller;

import com.app.loanapp.dto.MarkRepaymentRequest;
import com.app.loanapp.dto.PaymentResponse;
import com.app.loanapp.entity.Loan;
import com.app.loanapp.exception.NotFoundException;
import com.app.loanapp.repository.LoanRepository;
import com.app.loanapp.service.LoanService;
import com.app.loanapp.service.RepaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private RepaymentService repaymentService;

    @Autowired
    private LoanRepository loanRepository;

    @PostMapping("/loans")
    public ResponseEntity<Loan> createLoan(@RequestBody Loan loan) {
        Loan createdLoan = loanService.createLoan(loan);
        return ResponseEntity.ok(createdLoan);
    }

    @GetMapping("/loans/customer/{customerId}")
    public ResponseEntity<List<Loan>> getLoansByCustomerId(@PathVariable Long customerId) {
        List<Loan> loans = loanRepository.findByCustomerId(customerId);
        return ResponseEntity.ok(loans);
    }

    @PostMapping("/repayments/{loanId}/schedule")
    public ResponseEntity<List<Loan>> generateRepaymentSchedule(@PathVariable Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new NotFoundException("Loan not found with id: " + loanId));
        repaymentService.generateRepaymentSchedule(loan);
        List<Loan> loans = loanRepository.findByCustomerId(loan.getCustomer().getId());
        return ResponseEntity.ok(loans);
    }

    @PostMapping("/repayments/{repaymentId}/mark-paid")
    public ResponseEntity<PaymentResponse> markRepaymentAsPaid(@PathVariable Long repaymentId,
                                                               @RequestBody MarkRepaymentRequest request) {
        PaymentResponse response = repaymentService.markRepaymentAsPaid(repaymentId, request);
        return ResponseEntity.ok(response);
    }
}