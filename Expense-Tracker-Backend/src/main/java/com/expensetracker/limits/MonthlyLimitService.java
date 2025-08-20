package com.expensetracker.limits;

import com.expensetracker.dto.MonthlySummaryResponse;
import com.expensetracker.expenditure.Expenditure;
import com.expensetracker.expenditure.ExpenditureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
public class MonthlyLimitService {
    @Autowired
    private MonthlyLimitRepository monthlyLimitRepository;
    @Autowired
    private ExpenditureRepository expenditureRepository;

    public String setMonthlyLimit(String username, YearMonth yearMonth, double limit) {
        MonthlyLimit monthlyLimit = monthlyLimitRepository.findByUsernameAndYearMonth(username, yearMonth.toString())
                .orElse(new MonthlyLimit());

        monthlyLimit.setUsername(username);
        monthlyLimit.setYearMonth(yearMonth.toString());
        monthlyLimit.setLimitAmount(limit);

        monthlyLimitRepository.save(monthlyLimit);

        return "Monthly limit set successfully";
    }

    public MonthlySummaryResponse getMonthlySummary(String username, YearMonth yearMonth) {
        List<Expenditure> monthlyExpenses = expenditureRepository.findByUserAndYearMonth(username, yearMonth.toString());

        double totalSpent = monthlyExpenses.stream()
                .mapToDouble(Expenditure::getAmount)
                .sum();

        double limit = monthlyLimitRepository.findByUsernameAndYearMonth(username, yearMonth.toString())
                .map(MonthlyLimit::getLimitAmount)
                .orElse(0.0);

        return new MonthlySummaryResponse(yearMonth.toString(), limit, totalSpent, monthlyExpenses);
    }

}

