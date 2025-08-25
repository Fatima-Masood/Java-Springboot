package com.expensetracker.limits;

import com.expensetracker.dto.MonthlySummaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/expenditures/monthly")
@Slf4j
public class MonthlyLimitController {
    @Autowired
    private MonthlyLimitService monthlyLimitService;

    @PostMapping("/set-limit")
    public ResponseEntity<?> setMonthlyLimit(@RequestParam int year,
                                             @RequestParam int month,
                                             @RequestParam double limit,
                                             Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Login First.");
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        String username = authentication.getName();
        String message = monthlyLimitService.setMonthlyLimit(username, yearMonth, limit);

        return ResponseEntity.ok(message);
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<?> getMonthlySummary(@RequestParam int year,
                                               @RequestParam int month,
                                               Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Login First.");
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        String username = authentication.getName();
        MonthlySummaryResponse response = monthlyLimitService.getMonthlySummary(username, yearMonth);

        return ResponseEntity.ok(response);
    }

}
