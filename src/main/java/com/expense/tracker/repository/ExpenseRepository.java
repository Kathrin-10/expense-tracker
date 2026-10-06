package com.expense.tracker.repository;

import com.expense.tracker.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
            select e from Expense e
            join fetch e.category c
            where (:title is null or lower(e.title) like lower(concat('%', :title, '%')))
              and (:categoryId is null or c.id = :categoryId)
              and (:fromDate is null or e.expenseDate >= :fromDate)
              and (:toDate is null or e.expenseDate <= :toDate)
            """)
    Page<Expense> search(
            @Param("title") String title,
            @Param("categoryId") Long categoryId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    @Query("select coalesce(sum(e.amount), 0) from Expense e")
    BigDecimal totalAmount();

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.expenseDate between :fromDate and :toDate")
    BigDecimal totalBetween(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query("select coalesce(max(e.amount), 0) from Expense e")
    BigDecimal maxAmount();

    @Query("select coalesce(min(e.amount), 0) from Expense e")
    BigDecimal minAmount();

    @Query("""
            select c.id, c.name, coalesce(sum(e.amount), 0)
            from Expense e join e.category c
            group by c.id, c.name
            order by sum(e.amount) desc
            """)
    List<Object[]> totalsByCategory();

    long countByCategoryId(Long categoryId);

    @Query("""
            select function('year', e.expenseDate), function('month', e.expenseDate),
                   coalesce(sum(e.amount), 0), count(e)
            from Expense e
            group by function('year', e.expenseDate), function('month', e.expenseDate)
            order by function('year', e.expenseDate) desc, function('month', e.expenseDate) desc
            """)
    List<Object[]> monthlyTotals();

    @Query("""
            select e from Expense e
            join fetch e.category
            where e.expenseDate between :fromDate and :toDate
            order by e.expenseDate desc, e.id desc
            """)
    List<Expense> findForExport(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
}
