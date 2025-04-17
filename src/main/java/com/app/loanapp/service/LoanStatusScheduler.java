package com.app.loanapp.service;

import com.app.loanapp.entity.Loan;
import com.app.loanapp.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class LoanStatusScheduler {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private RepaymentService repaymentService;

    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    public void updateLoanStatuses() {
        List<Loan> activeLoans = loanRepository.findAll().stream()
                .filter(loan -> "ACTIVE".equals(loan.getStatus()))
                .toList();

        for (Loan loan : activeLoans) {
            repaymentService.checkAndUpdateLoanStatus(loan);
            loanRepository.save(loan);
        }
    }
}