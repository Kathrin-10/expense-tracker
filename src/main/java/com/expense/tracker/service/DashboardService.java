package com.expense.tracker.service;

import com.expense.tracker.dto.DashboardResponse;
import com.expense.tracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.*;

@Service
public class DashboardService {
    private final ExpenseRepository repo;
    public DashboardService(ExpenseRepository repo){this.repo=repo;}
    public DashboardResponse getDashboard(){
        YearMonth ym=YearMonth.now();
        BigDecimal total=repo.totalAmount(); long count=repo.count(); BigDecimal max=repo.maxAmount(); BigDecimal min=count==0?BigDecimal.ZERO:repo.minAmount(); BigDecimal current=repo.totalBetween(ym.atDay(1),ym.atEndOfMonth());
        var categories=repo.totalsByCategory().stream().map(r->new DashboardResponse.CategoryTotal((Long)r[0],(String)r[1],(BigDecimal)r[2])).toList();
        return new DashboardResponse(total,count,max,min,current,categories);
    }
}
