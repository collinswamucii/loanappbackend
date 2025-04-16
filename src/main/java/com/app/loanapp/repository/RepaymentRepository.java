package com.app.loanapp.repository;

import com.app.loanapp.entity.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findByLoanIdOrderByDueDateAsc(Long loanId);

    @Query("SELECT r FROM Repayment r WHERE r.loan.id = :loanId AND r.status = 'PENDING' ORDER BY r.dueDate ASC")
    Optional<Repayment> findFirstPendingByLoanId(Long loanId);
}