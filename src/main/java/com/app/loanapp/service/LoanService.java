package com.app.loanapp.service;

import com.app.loanapp.entity.Customer;
import com.app.loanapp.entity.Loan;
import com.app.loanapp.exception.NotFoundException;
import com.app.loanapp.repository.CustomerRepository;
import com.app.loanapp.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private RepaymentService repaymentService;
    @Transactional
    public Loan createLoan(Loan loan) {
        // Validate and retrieve the customer
        Long customerId = loan.getCustomer().getId();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + customerId));
        loan.setCustomer(customer);

        // Set default values
        if (loan.getStartDate() == null) {
            loan.setStartDate(new Date()); // Set to current date/time
        }
        if (loan.getStatus() == null) {
            loan.setStatus("ACTIVE"); // Default status for new loans
        }
        loan.setTotalPaid(0.0); // Initialize totalPaid to 0

        // Save the loan
        Loan savedLoan = loanRepository.save(loan);

        // Generate the repayment schedule to calculate totalAmountDue and remainingBalance
        repaymentService.generateRepaymentSchedule(savedLoan);

        // Reload the loan to ensure all fields are updated
        return loanRepository.findById(savedLoan.getId())
                .orElseThrow(() -> new NotFoundException("Loan not found with id: " + savedLoan.getId()));
    }
}