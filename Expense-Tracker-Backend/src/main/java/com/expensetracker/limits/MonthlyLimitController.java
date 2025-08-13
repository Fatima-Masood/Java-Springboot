package com.expensetracker.limits;

import com.expensetracker.dto.MonthlySummaryResponse;
import com.expensetracker.expenditure.Expenditure;
import com.expensetracker.expenditure.ExpenditureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/expenditures/monthly")
@Slf4j
public class MonthlyLimitController {
    @Autowired
    private MonthlyLimitRepository monthlyLimitRepository;
    @Autowired
    private ExpenditureRepository expenditureRepository;

    @GetMapping("/monthly-summary")
    public ResponseEntity<?> getMonthlySummary(@RequestParam int year,
                                               @RequestParam int month,
                                               Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Login First.");
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        String username = authentication.getName();

        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        List<Expenditure> monthlyExpenses = expenditureRepository.findByUserAndTimestampBetween(
                username, start, end);

        double totalSpent = monthlyExpenses.stream()
                .mapToDouble(Expenditure::getAmount)
                .sum();

        MonthlyLimit monthlyLimit = monthlyLimitRepository.findByUsernameAndMonth(username, yearMonth.toString())
                .orElse(null);

        MonthlySummaryResponse response = new MonthlySummaryResponse(yearMonth,
                                                monthlyLimit != null ? monthlyLimit.getLimitAmount() : 0,
                                                totalSpent, monthlyExpenses);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/set-limit")
    public ResponseEntity<?> setMonthlyLimit(@RequestParam String year,
                                             @RequestParam String month,
                                             @RequestParam double limit,
                                             Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Login First.");
        }

        String yearMonth = year + '-' + month;
        String username = authentication.getName();

        MonthlyLimit monthlyLimit = monthlyLimitRepository.findByUsernameAndMonth(username, yearMonth)
                .orElse(new MonthlyLimit());

        monthlyLimit.setUsername(username);
        monthlyLimit.setMonth(yearMonth);
        monthlyLimit.setLimitAmount(limit);

        monthlyLimitRepository.save(monthlyLimit);

        return ResponseEntity.ok("Monthly limit set successfully");
    }
}
