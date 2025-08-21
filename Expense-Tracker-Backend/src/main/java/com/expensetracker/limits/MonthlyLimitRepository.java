package com.expensetracker.limits;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MonthlyLimitRepository extends MongoRepository<MonthlyLimit, String> {
    Optional<MonthlyLimit> findByUsernameAndYearMonth(String username, String month);
    void deleteByUsername (String username);
}
