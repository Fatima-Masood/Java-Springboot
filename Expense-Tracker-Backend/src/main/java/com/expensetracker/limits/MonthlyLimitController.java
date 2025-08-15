package com.expensetracker.limits;

import com.expensetracker.dto.MonthlySummaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenditures/monthly")
@Slf4j
public class MonthlyLimitController {
    @Autowired
    private MonthlyLimitService monthlyLimitService;

    @GetMapping("/monthly-summary")
    public ResponseEntity<?> getMonthlySummary(@RequestParam int year,
                                               @RequestParam int month,
                                               Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Login First.");
        }

        String username = authentication.getName();
        MonthlySummaryResponse response = monthlyLimitService.getMonthlySummary(username, year, month);

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

        String username = authentication.getName();
        String message = monthlyLimitService.setMonthlyLimit(username, year, month, limit);

        return ResponseEntity.ok(message);
    }

}
