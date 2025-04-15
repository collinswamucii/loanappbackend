package com.app.loanapp.controller;

import com.app.loanapp.entity.Loan;
import com.app.loanapp.repository.CustomerRepository;
import com.app.loanapp.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping
    public ResponseEntity<Loan> createLoan(@RequestBody Loan loan) {
        return customerRepository.findById(loan.getCustomer().getId())
                .map(customer -> {
                    loan.setCustomer(customer);
                    loan.setStartDate(new Date());
                    loan.setStatus("ACTIVE");
                    loan.setTotalPaid(0.0);
                    return ResponseEntity.ok(loanRepository.save(loan));
                })
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/customer/{customerId}")
    public List<Loan> getLoansByCustomer(@PathVariable Long customerId) {
        return loanRepository.findByCustomerId(customerId);
    }
}