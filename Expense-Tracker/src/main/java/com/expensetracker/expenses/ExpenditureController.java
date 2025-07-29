package com.expensetracker.expenses;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenditures")
@RequiredArgsConstructor
@Slf4j
public class ExpenditureController {

    private final ExpenditureRepository expenditureRepository;

    @PostMapping
    public ResponseEntity<Expenditure> addExpenditure(
            @RequestBody Expenditure exp,
            Authentication authentication) {
        if (authentication != null ) {
            exp.setUser(authentication.getName());
            exp = expenditureRepository.save(exp);
            return ResponseEntity.ok(exp);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<Expenditure> updateExpenditure(@PathVariable String id,
                                                         @RequestBody Expenditure exp,
                                                         Authentication authentication) {
        if (exp.getUser().equals(authentication.getName())) {
            Expenditure existing = expenditureRepository.findById(id).orElseThrow(() -> new RuntimeException("Expenditure not found"));

            existing.setTitle(exp.getTitle());
            existing.setAmount(exp.getAmount());

            return ResponseEntity.ok(expenditureRepository.save(existing));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @DeleteMapping("/{exp}")
    public ResponseEntity<String> deleteExpenditure(@PathVariable Expenditure exp,
                                                    Authentication authentication) {
        if (exp.getUser().equals(authentication.getName())) {
            expenditureRepository.deleteById(exp.getId());
            return ResponseEntity.ok("Expenditure deleted");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping
    public ResponseEntity<List<Expenditure>> getExpendituresByUser(Authentication authentication) {
        if (authentication!= null && authentication.getName() != null) {
            return ResponseEntity.ok(expenditureRepository.findByUser(authentication.getName()));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}

