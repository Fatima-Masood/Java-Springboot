package com.expensetracker.limits;

import com.expensetracker.dto.MonthlySummaryResponse;
import com.expensetracker.expenditure.Expenditure;
import com.expensetracker.expenditure.ExpenditureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class MonthlyLimitService {
    @Autowired
    private MonthlyLimitRepository monthlyLimitRepository;
    @Autowired
    private ExpenditureRepository expenditureRepository;

    public String setMonthlyLimit(String username, String year, String month, double limit) {
        String yearMonth = year + '-' + month;

        MonthlyLimit monthlyLimit = monthlyLimitRepository.findByUsernameAndMonth(username, yearMonth)
                .orElse(new MonthlyLimit());

        monthlyLimit.setUsername(username);
        monthlyLimit.setMonth(yearMonth);
        monthlyLimit.setLimitAmount(limit);

        monthlyLimitRepository.save(monthlyLimit);

        return "Monthly limit set successfully";
    }

    public MonthlySummaryResponse getMonthlySummary(String username, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        List<Expenditure> monthlyExpenses = expenditureRepository.findByUserAndTimestampBetween(
                username, start, end);

        double totalSpent = monthlyExpenses.stream()
                .mapToDouble(Expenditure::getAmount)
                .sum();

        MonthlyLimit monthlyLimit = monthlyLimitRepository.findByUsernameAndMonth(username, yearMonth.toString())
                .orElse(null);

        return new MonthlySummaryResponse(yearMonth,
                monthlyLimit != null ? monthlyLimit.getLimitAmount() : 0,
                totalSpent,
                monthlyExpenses);
    }
}

