package com.expensetracker.expenses;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:8000")
@RestController
@RequestMapping("/api/expenditures")
@RequiredArgsConstructor
@Slf4j
public class ExpenditureController {

    private final ExpenditureRepository expenditureRepository;

    @PostMapping
    public ResponseEntity<?> addExpenditure(
            @RequestBody Expenditure exp,
            Authentication authentication) {
        if (exp.getTitle() == null || exp.getAmount() == 0){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("incomplete content");
        }
        if (authentication != null ) {
            exp.setUser(authentication.getName());
            exp = expenditureRepository.save(exp);
            return ResponseEntity.ok(exp);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpenditure(@PathVariable String id,
                                                         @RequestBody Expenditure exp,
                                                         Authentication authentication) {
        if (exp.getUser().equals(authentication.getName())) {
            Expenditure existing = expenditureRepository.findById(id).orElseThrow(() -> new RuntimeException("Expenditure not found"));

            existing.setTitle(exp.getTitle());
            existing.setAmount(exp.getAmount());

            return ResponseEntity.ok(expenditureRepository.save(existing));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
    }

    @DeleteMapping("/{exp}")
    public ResponseEntity<String> deleteExpenditure(@PathVariable Expenditure exp,
                                                    Authentication authentication) {
        if (exp.getUser().equals(authentication.getName())) {
            expenditureRepository.deleteById(exp.getId());
            return ResponseEntity.ok("Expenditure deleted");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
    }

    @GetMapping
    public ResponseEntity<?> getExpendituresByUser(Authentication authentication) {
        if (authentication!= null && authentication.getName() != null) {
            List<Expenditure> data = expenditureRepository.findByUser(authentication.getName());
            log.info(data.toString());
            return ResponseEntity.ok(data);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
    }
}

