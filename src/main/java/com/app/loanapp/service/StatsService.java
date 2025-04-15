package com.app.loanapp.service;

import com.app.loanapp.entity.Loan;
import com.app.loanapp.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatsService {
    @Autowired
    private LoanRepository loanRepository;

    public Map<String, Object> getLoanStats() {
       Map<String, Object> stats = new HashMap<>();
        double totalDisbursed = loanRepository.findAll().stream()
                .mapToDouble(Loan::getPrincipalAmount)
                .sum();
        double totalPaid = loanRepository.findAll().stream()
                .mapToDouble(Loan::getTotalPaid)
                .sum();
        long activeLoans = loanRepository.findAll().stream()
                .filter(loan -> "ACTIVE".equals(loan.getStatus()))
                .count();

        stats.put("totalDisbursed", totalDisbursed);
        stats.put("totalPaid", totalPaid);
        stats.put("activeLoans", activeLoans);
        return stats;
    }
}
