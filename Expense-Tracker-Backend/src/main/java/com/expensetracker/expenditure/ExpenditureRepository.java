package com.expensetracker.expenditure;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ExpenditureRepository extends MongoRepository<Expenditure, String> {
    void deleteByUser(String user);
    List<Expenditure> findByUser(String user);
    List<Expenditure> findByUserAndYearMonth(String user, String month);
}
