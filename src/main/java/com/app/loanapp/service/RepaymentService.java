package com.app.loanapp.service;

import com.app.loanapp.dto.MarkRepaymentRequest;
import com.app.loanapp.dto.PaymentResponse;
import com.app.loanapp.entity.Loan;
import com.app.loanapp.entity.Payment;
import com.app.loanapp.entity.Repayment;
import com.app.loanapp.exception.NotFoundException;
import com.app.loanapp.repository.LoanRepository;
import com.app.loanapp.repository.PaymentRepository;
import com.app.loanapp.repository.RepaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class RepaymentService {
    @Autowired
    private RepaymentRepository repaymentRepository;
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    public List<Repayment> generateRepaymentSchedule(Loan loan) {
        double principal = loan.getPrincipalAmount();
        double annualInterestRate = loan.getInterestRate();
        int numberOfPayments = loan.getRepaymentPeriod();
        String frequency = loan.getFrequency();

        // Calculate the interest rate per period based on frequency
        double interestRatePerPeriod;
        int periodsPerYear;
        switch (frequency) {
            case "WEEKLY":
                periodsPerYear = 52; // 52 weeks in a year
                interestRatePerPeriod = annualInterestRate / 100 / periodsPerYear;
                break;
            case "MONTHLY":
                periodsPerYear = 12; // 12 months in a year
                interestRatePerPeriod = annualInterestRate / 100 / periodsPerYear;
                break;
            case "YEARLY":
                periodsPerYear = 1; // 1 year
                interestRatePerPeriod = annualInterestRate / 100 / periodsPerYear;
                break;
            default:
                throw new IllegalArgumentException("Invalid frequency: " + frequency);
        }

        // Calculate payment per period using the amortization formula
        double paymentPerPeriod = (principal * interestRatePerPeriod * Math.pow(1 + interestRatePerPeriod, numberOfPayments)) /
                (Math.pow(1 + interestRatePerPeriod, numberOfPayments) - 1);

        // Round the payment to 2 decimal places
        paymentPerPeriod = Math.round(paymentPerPeriod * 100.0) / 100.0;

        // Calculate total amount due (principal + interest)
        double totalAmountDue = paymentPerPeriod * numberOfPayments;
        loan.setTotalAmountDue(totalAmountDue); // Update the loan with total amount due
        loan.setRemainingBalance(totalAmountDue); // Initialize remaining balance

        // Save the updated loan
        loanRepository.save(loan);

        List<Repayment> repayments = new ArrayList<>();
        Date startDate = loan.getStartDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        // Track remaining principal for amortization
        double remainingPrincipal = principal;

        for (int i = 0; i < numberOfPayments; i++) {
            // Calculate interest for this period
            double interestForPeriod = remainingPrincipal * interestRatePerPeriod;
            interestForPeriod = Math.round(interestForPeriod * 100.0) / 100.0;

            // Calculate principal for this period
            double principalForPeriod = paymentPerPeriod - interestForPeriod;
            principalForPeriod = Math.round(principalForPeriod * 100.0) / 100.0;

            // Update remaining principal
            remainingPrincipal -= principalForPeriod;
            remainingPrincipal = Math.round(remainingPrincipal * 100.0) / 100.0;

            // Handle the last payment to avoid rounding errors
            if (i == numberOfPayments - 1 && remainingPrincipal > 0) {
                principalForPeriod += remainingPrincipal;
                paymentPerPeriod = principalForPeriod + interestForPeriod;
                remainingPrincipal = 0;
            }

            Repayment repayment = new Repayment();
            repayment.setLoan(loan);
            repayment.setPrincipalAmount(principalForPeriod);
            repayment.setInterestAmount(interestForPeriod);
            repayment.setAmount(paymentPerPeriod);
            repayment.setStatus("PENDING");

            // Set due date based on frequency
            switch (frequency) {
                case "WEEKLY":
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case "MONTHLY":
                    calendar.add(Calendar.MONTH, 1);
                    break;
                case "YEARLY":
                    calendar.add(Calendar.YEAR, 1);
                    break;
            }
            repayment.setDueDate(calendar.getTime());

            repayments.add(repayment);
        }

        return repaymentRepository.saveAll(repayments);
    }

    @Transactional
    public PaymentResponse markRepaymentAsPaid(Long repaymentId, MarkRepaymentRequest request) {
        // Find the repayment
        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new NotFoundException("Repayment not found with id: " + repaymentId));

        // Check if already paid
        if ("PAID".equals(repayment.getStatus())) {
            throw new IllegalStateException("Repayment is already marked as paid");
        }

        // Validate payment reference based on method
        String paymentRef = request.getRef();
        if ("CASH".equals(request.getMethod())) {
            if (paymentRef == null || paymentRef.trim().isEmpty()) {
                paymentRef = "CASH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            }
        } else if (("MPESA".equals(request.getMethod()) || "BANK".equals(request.getMethod())) &&
                (paymentRef == null || paymentRef.trim().isEmpty())) {
            throw new IllegalArgumentException("Payment reference is required for MPESA and BANK payments");
        }

        // Mark the repayment as paid
        repayment.setStatus("PAID");
        repaymentRepository.save(repayment);

        // Update the corresponding loan
        Loan loan = repayment.getLoan();
        loan.setTotalPaid(loan.getTotalPaid() + request.getAmount());
        loan.setRemainingBalance(loan.getTotalAmountDue() - loan.getTotalPaid());

        // Update loan status to PAID if fully paid
        if (loan.getTotalPaid() >= loan.getTotalAmountDue()) {
            loan.setStatus("PAID");
        }

        // Check for defaulted status
        checkAndUpdateLoanStatus(loan);

        // Save the updated loan
        loanRepository.save(loan);

        // Record the payment
        Payment payment = new Payment();
        payment.setRepayment(repayment);
        payment.setLoan(loan);
        payment.setPaymentRef(paymentRef);
        payment.setPaymentMethod(request.getMethod());
        payment.setAmount(request.getAmount());
        payment.setDatePaid(new Date()); // Auto-fill with current date
        paymentRepository.save(payment);

        // Prepare the response
        PaymentResponse response = new PaymentResponse();
        response.setRepaymentId(repaymentId);
        response.setAmount(request.getAmount());
        response.setMethod(request.getMethod());
        response.setRef(paymentRef);
        response.setDate(payment.getDatePaid());

        return response;
    }

    public void checkAndUpdateLoanStatus(Loan loan) {
        // Skip if the loan is already PAID
        if ("PAID".equals(loan.getStatus())) {
            return;
        }

        // Check for any past-due repayments
        List<Repayment> repayments = repaymentRepository.findByLoanIdOrderByDueDateAsc(loan.getId());
        Date currentDate = new Date();
        for (Repayment repayment : repayments) {
            if ("PENDING".equals(repayment.getStatus()) && repayment.getDueDate().before(currentDate)) {
                loan.setStatus("DEFAULTED");
                return;
            }
        }

        // If no past-due repayments, ensure status is ACTIVE
        if (!"DEFAULTED".equals(loan.getStatus())) {
            loan.setStatus("ACTIVE");
        }
    }
}