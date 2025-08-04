package com.expensetracker.expenditure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
            Optional<Expenditure> existing = expenditureRepository.findById(id);

            if (existing.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Does not exist");
            }

            Expenditure expenditure = existing.get();
            expenditure.setTitle(exp.getTitle());
            expenditure.setAmount(exp.getAmount());

            return ResponseEntity.ok(expenditureRepository.save(expenditure));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExpenditure(@PathVariable String id, Authentication authentication) {
        Optional<Expenditure> existing = expenditureRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Does not exist");
        }

        Expenditure exp = existing.get();
        if (!exp.getUser().equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        expenditureRepository.deleteById(id);
        return ResponseEntity.ok("Expenditure deleted");
    }


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getExpendituresByUser(Authentication authentication) {
        if (authentication!= null && authentication.getName() != null) {
            List<Expenditure> data = expenditureRepository.findByUser(authentication.getName());
            return ResponseEntity.ok(data);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Login First.");
    }
}

