package com.expensetracker.expenditure;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "expenditure")
public class Expenditure {

    @Id
    private String id;
    private String user;
    private String title;
    private double amount = 0;
    private String yearMonth;
    private LocalDateTime timestamp = LocalDateTime.now();

    public Expenditure(Expenditure expenditure) {
        if (expenditure != null) {
            this.id = expenditure.id;
            this.user = expenditure.user;
            this.title = expenditure.title;
            this.amount = expenditure.amount;
            this.yearMonth = expenditure.yearMonth;
        }
        this.timestamp = LocalDateTime.now();

    }
}