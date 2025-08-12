package com.expensetracker.expenditure;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpenditureRepository extends MongoRepository<Expenditure, String> {
    void deleteByUser(String user);
    List<Expenditure> findByUser(String user);
    List<Expenditure> findByUserAndTimestampBetween(String user, LocalDateTime start, LocalDateTime end);
}
