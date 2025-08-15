package com.expensetracker.expenditure;

import com.expensetracker.dto.ExpenditureDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/expenditures")
@Slf4j
public class ExpenditureController {

    @Autowired
    private ExpenditureService expenditureService;

    @PostMapping
    public ResponseEntity<?> addExpenditure(@RequestBody ExpenditureDTO request,
                                            Authentication authentication) {

        if (request.getTitle() == null || request.getAmount() == 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("incomplete content");
        }

        if (authentication != null) {
            return ResponseEntity.ok(
                    expenditureService.addExpenditure(authentication.getName(), request)
            );
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpenditure(@PathVariable String id,
                                               @RequestBody ExpenditureDTO dto,
                                               Authentication authentication) {

        try {
            Optional<Expenditure> updated = expenditureService.updateExpenditure(authentication.getName(), id, dto);
            if (updated.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Does not exist");
            }
            return ResponseEntity.ok(updated.get());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExpenditure(@PathVariable String id,
                                                    Authentication authentication) {
        try {
            boolean deleted = expenditureService.deleteExpenditure(authentication.getName(), id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Does not exist");
            }
            return ResponseEntity.ok("Expenditure deleted");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getExpendituresByUser(Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            List<Expenditure> data = expenditureService.getExpendituresByUser(authentication.getName());
            return ResponseEntity.ok(data);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Login First.");
    }
}
