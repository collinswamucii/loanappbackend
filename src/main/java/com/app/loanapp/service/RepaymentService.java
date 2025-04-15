package com.app.loanapp.service;

import com.app.loanapp.entity.Loan;
import com.app.loanapp.entity.Repayment;
import com.app.loanapp.repository.RepaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RepaymentService {
    @Autowired
    private RepaymentRepository repaymentRepository;

    public List<Repayment> generateRepaymentSchedule(Loan loan) {
        List<Repayment> schedule = new ArrayList<>();
        double monthlyPayment = calculateMonthlyPayment(
                loan.getPrincipalAmount(), loan.getInterestRate(), loan.getRepaymentPeriod()
        );
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(loan.getStartDate());

        for (int i = 0; i < loan.getRepaymentPeriod(); i++) {
            calendar.add(Calendar.MONTH, 1); // Assuming monthly frequency for simplicity
            Repayment repayment = new Repayment();
            repayment.setLoan(loan);
            repayment.setAmount(monthlyPayment);
            repayment.setDueDate(calendar.getTime());
            repayment.setStatus("PENDING");
            schedule.add(repayment);
        }
        return repaymentRepository.saveAll(schedule);
    }

    private double calculateMonthlyPayment(double principal, double annualRate, int months) {
        double monthlyRate = annualRate / 100 / 12;
        return principal * (monthlyRate * Math.pow(1 + monthlyRate, months)) /
                (Math.pow(1 + monthlyRate, months) - 1);
    }
}
