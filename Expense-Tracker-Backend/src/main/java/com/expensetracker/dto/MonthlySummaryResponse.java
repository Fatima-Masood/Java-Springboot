package com.expensetracker.dto;

import com.expensetracker.expenditure.Expenditure;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MonthlySummaryResponse {
    private String yearMonth;
    private double limitAmount;
    private double totalSpent;
    private List<Expenditure> expenses = new ArrayList<>();

    public MonthlySummaryResponse(String month, double v, double v1, List<Expenditure> expenses) {
        setYearMonth(month);
        setLimitAmount(v);
        setTotalSpent(v1);
        setExpenses(expenses);
    }

    public void setExpenses(List<Expenditure> expenses) {
        this.expenses = expenses == null ? null : new ArrayList<>(expenses);
    }

    public List<Expenditure> getExpenses() {
        return expenses == null ? null : new ArrayList<>(expenses);
    }
}
