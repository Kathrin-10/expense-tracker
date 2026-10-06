package com.expense.tracker.controller;

import com.expense.tracker.entity.Expense;
import com.expense.tracker.service.ExpenseService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.List;

@RestController
@RequestMapping("/api/expenses/export")
public class ExportController {
    private final ExpenseService service;
    public ExportController(ExpenseService service){this.service=service;}
    @GetMapping(value="/csv", produces="text/csv")
    public ResponseEntity<byte[]> csv(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fromDate,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate toDate){
        LocalDate from=fromDate==null?LocalDate.of(2000,1,1):fromDate; LocalDate to=toDate==null?LocalDate.now():toDate;
        List<Expense> expenses=service.export(from,to); StringBuilder csv=new StringBuilder("ID,Title,Amount,Date,Category,Description\n");
        for(Expense e:expenses) csv.append(e.getId()).append(',').append(q(e.getTitle())).append(',').append(e.getAmount()).append(',').append(e.getExpenseDate()).append(',').append(q(e.getCategory().getName())).append(',').append(q(e.getDescription())).append('\n');
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=expense-report.csv").contentType(MediaType.parseMediaType("text/csv")).body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }
    private String q(String s){return "\""+(s==null?"":s.replace("\"","\"\""))+"\"";}
}
