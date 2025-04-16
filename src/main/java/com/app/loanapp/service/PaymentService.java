package com.app.loanapp.service;

import com.app.loanapp.dto.PaymentMessage;
import com.app.loanapp.entity.Customer;
import com.app.loanapp.entity.Loan;
import com.app.loanapp.entity.Payment;
import com.app.loanapp.entity.Repayment;
import com.app.loanapp.repository.CustomerRepository;
import com.app.loanapp.repository.LoanRepository;
import com.app.loanapp.repository.PaymentRepository;
import com.app.loanapp.repository.RepaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class PaymentService {
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private RepaymentRepository repaymentRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public Loan recordPayment(String phoneNumber, PaymentMessage paymentMessage) {
        double paymentAmount = paymentMessage.getAmount();

        // Validate payment amount
        if (paymentAmount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }

        // Find the customer by phone number
        Customer customer = customerRepository.findByPhone(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with phone number: " + phoneNumber));

        // Find all active loans for the customer
        List<Loan> activeLoans = loanRepository.findByCustomerId(customer.getId()).stream()
                .filter(loan -> "ACTIVE".equals(loan.getStatus()))
                .toList();

        if (activeLoans.isEmpty()) {
            throw new IllegalArgumentException("No active loans found for customer with phone number: " + phoneNumber);
        }

        // Select the loan with the soonest due date
        Loan selectedLoan = activeLoans.stream()
                .filter(loan -> repaymentRepository.findFirstPendingByLoanId(loan.getId()).isPresent())
                .min(Comparator.comparing(loan -> repaymentRepository.findFirstPendingByLoanId(loan.getId()).get().getDueDate()))
                .orElseThrow(() -> new IllegalArgumentException("No loans with pending repayments found for customer with phone number: " + phoneNumber));

        // Update totalPaid
        selectedLoan.setTotalPaid(selectedLoan.getTotalPaid() + paymentAmount);

        // Fetch all repayments for the loan, ordered by due date
        List<Repayment> repayments = repaymentRepository.findByLoanIdOrderByDueDateAsc(selectedLoan.getId());

        // Apply payment to pending repayments
        double remainingPayment = paymentAmount;
        for (Repayment repayment : repayments) {
            if ("PENDING".equals(repayment.getStatus()) && remainingPayment > 0) {
                double amountDue = repayment.getAmount();
                if (remainingPayment >= amountDue) {
                    repayment.setStatus("PAID");
                    remainingPayment -= amountDue;
                } else {
                    // Partial payment: leave as PENDING (you can extend this logic if needed)
                    break;
                }
            }
        }

        // Save updated repayments
        repaymentRepository.saveAll(repayments);

        // Calculate total amount due (sum of all repayments)
        double totalAmountDue = repayments.stream()
                .mapToDouble(Repayment::getAmount)
                .sum();

        // Update loan status if fully paid
        if (selectedLoan.getTotalPaid() >= totalAmountDue) {
            selectedLoan.setStatus("PAID");
        }

        // Save the updated loan
        selectedLoan = loanRepository.save(selectedLoan);

        // Create and save the payment record
        Payment payment = new Payment();
        payment.setPaymentRef(paymentMessage.getPaymentRef());
        payment.setAmount(paymentAmount);
        payment.setDatePaid(paymentMessage.getDatePaid());
        payment.setLoan(selectedLoan);
        paymentRepository.save(payment);

        return selectedLoan;
    }
}