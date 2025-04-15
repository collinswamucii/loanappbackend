package com.app.loanapp.controller;

import com.app.loanapp.entity.Repayment;
import com.app.loanapp.repository.LoanRepository;
import com.app.loanapp.service.RepaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repayments")
public class RepaymentController {
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private RepaymentService repaymentService;

    @PostMapping("/{loanId}/schedule")
    public List<Repayment> generateSchedule(@PathVariable Long loanId) {
        return loanRepository.findById(loanId)
                .map(loan -> repaymentService.generateRepaymentSchedule(loan))
                .orElseThrow(() -> new RuntimeException("Loan not found"));
    }
}