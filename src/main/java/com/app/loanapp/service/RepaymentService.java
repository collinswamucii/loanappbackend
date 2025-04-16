package com.app.loanapp.service;

import com.app.loanapp.entity.Loan;
import com.app.loanapp.entity.Repayment;
import com.app.loanapp.repository.LoanRepository;
import com.app.loanapp.repository.RepaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class RepaymentService {
    @Autowired
    private RepaymentRepository repaymentRepository;
    @Autowired
    private LoanRepository loanRepository;

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
}