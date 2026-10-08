package com.expense.tracker.repository;

import com.expense.tracker.entity.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

  @Query(
      "select e from Expense e join fetch e.category c "
          + "where e.user.id = :userId "
          + "and (:title is null or lower(e.title) like lower(concat('%', :title, '%'))) "
          + "and (:categoryId is null or c.id = :categoryId) "
          + "and (:fromDate is null or e.expenseDate >= :fromDate) "
          + "and (:toDate is null or e.expenseDate <= :toDate)")
  Page<Expense> search(
      @Param("userId") Long userId,
      @Param("title") String title,
      @Param("categoryId") Long categoryId,
      @Param("fromDate") LocalDate fromDate,
      @Param("toDate") LocalDate toDate,
      Pageable pageable);

  @Query(
      "select coalesce(sum(e.amount), 0) from Expense e "
          + "where e.user.id = :userId "
          + "and (:title is null or lower(e.title) like lower(concat('%', :title, '%'))) "
          + "and (:categoryId is null or e.category.id = :categoryId) "
          + "and (:fromDate is null or e.expenseDate >= :fromDate) "
          + "and (:toDate is null or e.expenseDate <= :toDate)")
  BigDecimal searchTotal(
      @Param("userId") Long userId,
      @Param("title") String title,
      @Param("categoryId") Long categoryId,
      @Param("fromDate") LocalDate fromDate,
      @Param("toDate") LocalDate toDate);

  @Query("select coalesce(sum(e.amount), 0) from Expense e where e.user.id = :userId")
  BigDecimal totalAmountByUserId(@Param("userId") Long userId);

  @Query("select count(e) from Expense e where e.user.id = :userId")
  long countByUserId(@Param("userId") Long userId);

  @Query("select coalesce(max(e.amount), 0) from Expense e where e.user.id = :userId")
  BigDecimal maxAmount(@Param("userId") Long userId);

  @Query("select coalesce(min(e.amount), 0) from Expense e where e.user.id = :userId")
  BigDecimal minAmount(@Param("userId") Long userId);

  @Query(
      "select coalesce(sum(e.amount), 0) from Expense e "
          + "where e.user.id = :userId and e.expenseDate between :fromDate and :toDate")
  BigDecimal totalBetween(
      @Param("userId") Long userId,
      @Param("fromDate") LocalDate fromDate,
      @Param("toDate") LocalDate toDate);

  @Query(
      "select c.id, c.name, coalesce(sum(e.amount), 0) "
          + "from Expense e join e.category c where e.user.id = :userId "
          + "group by c.id, c.name order by sum(e.amount) desc")
  List<Object[]> totalsByCategory(@Param("userId") Long userId);

  @Query("select count(e) from Expense e where e.category.id = :categoryId and e.user.id = :userId")
  long countByCategoryIdAndUserId(
      @Param("categoryId") Long categoryId, @Param("userId") Long userId);

  @Query(
      "select function('year', e.expenseDate), function('month', e.expenseDate), "
          + "coalesce(sum(e.amount), 0), count(e) from Expense e "
          + "where e.user.id = :userId "
          + "group by function('year', e.expenseDate), function('month', e.expenseDate) "
          + "order by function('year', e.expenseDate) desc, function('month', e.expenseDate) desc")
  List<Object[]> monthlyTotals(@Param("userId") Long userId);

  @Query("select e from Expense e where e.user is null")
  List<Expense> findUnassigned();

  @Query(
      "select e from Expense e join fetch e.category "
          + "where e.user.id = :userId and e.expenseDate between :fromDate and :toDate "
          + "order by e.expenseDate desc, e.id desc")
  List<Expense> findForExport(
      @Param("userId") Long userId,
      @Param("fromDate") LocalDate fromDate,
      @Param("toDate") LocalDate toDate);
}
