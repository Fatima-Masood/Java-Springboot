package com.expensetracker.limits;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "monthly_limits")
@CompoundIndex(name = "unique_user_month", def = "{'username': 1, 'month': 1}", unique = true)
public class MonthlyLimit {
    @Id
    private String id;
    private String username;
    private String yearMonth;
    private double limitAmount;
}
