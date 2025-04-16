package com.app.loanapp.controller;

import com.app.loanapp.entity.Loan;
import com.app.loanapp.repository.CustomerRepository;
import com.app.loanapp.repository.LoanRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Loan> createLoan(@Valid @RequestBody Loan loan) {
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
    public ResponseEntity<List<Loan>> getLoansByCustomer(@PathVariable Long customerId) {
        List<Loan> loans = loanRepository.findByCustomerId(customerId);
        if (loans.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
            // OR
            // return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404 Not Found
        }
        return new ResponseEntity<>(loans, HttpStatus.OK); // 200 OK
    }
}