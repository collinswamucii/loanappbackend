package com.app.loanapp.service;

import com.app.loanapp.dto.LoanBalanceResponse;
import com.app.loanapp.entity.Customer;
import com.app.loanapp.entity.Loan;
import com.app.loanapp.exception.NotFoundException;
import com.app.loanapp.repository.CustomerRepository;
import com.app.loanapp.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanRepository loanRepository;

    public Customer createCustomer(Customer customer) {
        if (customerRepository.findByPhone(customer.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + id));
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Customer existingCustomer = getCustomerById(id);
        if (!existingCustomer.getPhone().equals(updatedCustomer.getPhone()) &&
                customerRepository.findByPhone(updatedCustomer.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        existingCustomer.setFirstName(updatedCustomer.getFirstName());
        existingCustomer.setLastName(updatedCustomer.getLastName());
        existingCustomer.setEmail(updatedCustomer.getEmail());
        existingCustomer.setPhone(updatedCustomer.getPhone());
        return customerRepository.save(existingCustomer);
    }

    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        List<Loan> loans = loanRepository.findByCustomerId(id);
        List<LoanBalanceResponse> activeLoanBalances = loans.stream()
                .filter(loan -> loan.getRemainingBalance() > 0)  // Consider a loan "active" if remaining balance > 0
                .map(loan -> new LoanBalanceResponse(loan.getId(), BigDecimal.valueOf(loan.getRemainingBalance())))
                .collect(Collectors.toList());
        if (!activeLoanBalances.isEmpty()) {
            throw new IllegalStateException("Cannot delete customer with active loans. Loan balances: " + activeLoanBalances);
        }
        customerRepository.delete(customer);
    }
}