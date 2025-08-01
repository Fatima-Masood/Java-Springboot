package com.expensetracker.expenses;

import com.expensetracker.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "expenses")
public class Expenditure {

    @Id
    private String id;
    private String user;
    private String title;
    private double amount = 0;
    private LocalDateTime timestamp = LocalDateTime.now();
}