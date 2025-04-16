package com.app.loanapp.controller;

import com.app.loanapp.entity.Repayment;
import com.app.loanapp.exception.NotFoundException;
import com.app.loanapp.repository.LoanRepository;
import com.app.loanapp.repository.RepaymentRepository;
import com.app.loanapp.service.RepaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repayments")
public class RepaymentController {
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private RepaymentService repaymentService;
    @Autowired
    private RepaymentRepository repaymentRepository;

    @PostMapping("/{loanId}/schedule")
    public ResponseEntity<List<Repayment>> generateSchedule(@PathVariable Long loanId) {
        return ResponseEntity.ok(
                loanRepository.findById(loanId)
                        .map(repaymentService::generateRepaymentSchedule)
                        .orElseThrow(() -> new NotFoundException("Loan not found with ID: " + loanId))
        );
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<Repayment>> getRepaymentsByLoan(@PathVariable Long loanId) {
        List<Repayment> repayments = repaymentRepository.findByLoanIdOrderByDueDateAsc(loanId);
        if (repayments.isEmpty()) {
            throw new NotFoundException("No repayments found for loan with ID: " + loanId);
        }
        return ResponseEntity.ok(repayments);
    }
}