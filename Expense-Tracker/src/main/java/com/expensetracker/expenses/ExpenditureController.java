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
@RequestMapping("/api/user/{user}/expenditures")
@RequiredArgsConstructor
@Slf4j
public class ExpenditureController {

    private final ExpenditureRepository expenditureRepository;

    @PostMapping
    public ResponseEntity<Expenditure> addExpenditure(
            @PathVariable String user,
            @RequestBody Expenditure exp,
            Authentication authentication) {
        if (!user.equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        exp.setUser(user);
        return ResponseEntity.ok(expenditureRepository.save(exp));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Expenditure> updateExpenditure(@PathVariable String user,
                                                         @PathVariable String id,
                                                         @RequestBody Expenditure exp,
                                                            Authentication authentication) {
        if (!user.equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Expenditure existing = expenditureRepository.findById(id).orElseThrow(() -> new RuntimeException("Expenditure not found"));
        existing.setTitle(exp.getTitle());
        existing.setAmount(exp.getAmount());

        return ResponseEntity.ok(expenditureRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExpenditure(@PathVariable String user,
                                                    @PathVariable String id,
                                                    Authentication authentication) {
        if (!user.equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        expenditureRepository.deleteById(id);
        return ResponseEntity.ok("Expenditure deleted");
    }

    @GetMapping
    public ResponseEntity<List<Expenditure>> getExpendituresByUser(@PathVariable String user,
                                                                   Authentication authentication) {
        if (!user.equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(expenditureRepository.findByUser(user));
    }
}

