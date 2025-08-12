package com.expensetracker.limits;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MonthlyLimitRepository extends MongoRepository<MonthlyLimit, String> {
    Optional<MonthlyLimit> findByUsernameAndMonth(String username, String month);

}
