package com.expense.tracker.service;

import com.expense.tracker.dto.MonthlyReportResponse;
import com.expense.tracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Month;
import java.util.*;

@Service
public class ReportService {
    private final ExpenseRepository repo;
    public ReportService(ExpenseRepository repo){this.repo=repo;}
    public List<MonthlyReportResponse> monthly(){
        return repo.monthlyTotals().stream().map(r->{int y=((Number)r[0]).intValue();int m=((Number)r[1]).intValue();return new MonthlyReportResponse(y,m,Month.of(m).name(),(BigDecimal)r[2],((Number)r[3]).longValue());}).toList();
    }
}
