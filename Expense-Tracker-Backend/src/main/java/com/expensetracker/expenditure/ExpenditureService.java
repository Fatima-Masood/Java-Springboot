package com.expensetracker.expenditure;

import com.expensetracker.dto.ExpenditureDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExpenditureService {

    @Autowired
    private ExpenditureRepository expenditureRepository;

    public Expenditure addExpenditure(String username, ExpenditureDTO request) {
        Expenditure exp = new Expenditure();
        exp.setTitle(request.getTitle());
        exp.setAmount(request.getAmount());
        exp.setUser(username);
        return expenditureRepository.save(exp);
    }

    public Optional<Expenditure> updateExpenditure(String username, String id, ExpenditureDTO dto) {
        Optional<Expenditure> existing = expenditureRepository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        Expenditure expenditure = existing.get();
        if (!expenditure.getUser().equals(username)) {
            throw new SecurityException("Forbidden");
        }

        expenditure.setTitle(dto.getTitle());
        expenditure.setAmount(dto.getAmount());

        return Optional.of(expenditureRepository.save(expenditure));
    }

    public boolean deleteExpenditure(String username, String id) {
        Optional<Expenditure> existing = expenditureRepository.findById(id);
        if (existing.isEmpty()) {
            return false;
        }

        Expenditure exp = existing.get();
        if (!exp.getUser().equals(username)) {
            throw new SecurityException("Forbidden");
        }

        expenditureRepository.deleteById(id);
        return true;
    }

    public List<Expenditure> getExpendituresByUser(String username) {
        return expenditureRepository.findByUser(username);
    }
}
