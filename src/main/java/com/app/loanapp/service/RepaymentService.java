package com.app.loanapp.service;

import com.app.loanapp.entity.Loan;
import com.app.loanapp.entity.Repayment;
import com.app.loanapp.repository.RepaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class RepaymentService {
    @Autowired
    private RepaymentRepository repaymentRepository;

    public List<Repayment> generateRepaymentSchedule(Loan loan) {
        List<Repayment> schedule = new ArrayList<>();
        double paymentAmount = calculatePaymentAmount(
                loan.getPrincipalAmount(), loan.getInterestRate(), loan.getRepaymentPeriod(), loan.getFrequency()
        );
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(loan.getStartDate());

        for (int i = 0; i < loan.getRepaymentPeriod(); i++) {
            // Adjust the due date based on the frequency
            switch (loan.getFrequency().toUpperCase()) {
                case "WEEKLY":
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case "MONTHLY":
                    calendar.add(Calendar.MONTH, 1);
                    break;
                case "YEARLY":
                    calendar.add(Calendar.YEAR, 1);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid repayment frequency: " + loan.getFrequency());
            }

            Repayment repayment = new Repayment();
            repayment.setLoan(loan);
            repayment.setAmount(paymentAmount);
            repayment.setDueDate(calendar.getTime());
            repayment.setStatus("PENDING");
            schedule.add(repayment);
        }

        return repaymentRepository.saveAll(schedule);
    }

    private double calculatePaymentAmount(double principal, double annualRate, int periods, String frequency) {
        // Convert annual rate to the rate per period based on frequency
        double ratePerPeriod = switch (frequency.toUpperCase()) {
            case "WEEKLY" -> annualRate / 100 / 52; // 52 weeks in a year
            case "MONTHLY" -> annualRate / 100 / 12; // 12 months in a year
            case "YEARLY" -> annualRate / 100; // 1 year
            default -> throw new IllegalArgumentException("Invalid repayment frequency: " + frequency);
        };

        // Calculate the payment amount using the formula for an annuity
        return principal * (ratePerPeriod * Math.pow(1 + ratePerPeriod, periods)) /
                (Math.pow(1 + ratePerPeriod, periods) - 1);
    }
}