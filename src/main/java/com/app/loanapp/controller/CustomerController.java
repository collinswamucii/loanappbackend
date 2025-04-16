package com.app.loanapp.controller;

import com.app.loanapp.entity.Customer;
import com.app.loanapp.exception.NotFoundException;
import com.app.loanapp.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        // Check if the phone number already exists
        if (customerRepository.findByPhone(customer.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        return ResponseEntity.ok(customerRepository.save(customer));
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(
                customerRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Customer not found with ID: " + id))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @Valid @RequestBody Customer updatedCustomer) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with ID: " + id));

        // Check if the new phone number is already taken by another customer
        if (!customer.getPhone().equals(updatedCustomer.getPhone()) &&
                customerRepository.findByPhone(updatedCustomer.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        customer.setFirstName(updatedCustomer.getFirstName());
        customer.setLastName(updatedCustomer.getLastName());
        customer.setEmail(updatedCustomer.getEmail());
        customer.setPhone(updatedCustomer.getPhone());
        return ResponseEntity.ok(customerRepository.save(customer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        if (!customerRepository.existsById(id)) {
            throw new NotFoundException("Customer not found with ID: " + id);
        }
        customerRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}