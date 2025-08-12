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

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/expenditures")
@Slf4j
public class ExpenditureController {
    @Autowired
    private ExpenditureRepository expenditureRepository;

    @PostMapping
    public ResponseEntity<?> addExpenditure(
            @RequestBody ExpenditureDTO request,
            Authentication authentication) {

        if (request.getTitle() == null || request.getAmount() == 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("incomplete content");
        }

        if (authentication != null) {
            Expenditure exp = new Expenditure();
            exp.setTitle(request.getTitle());
            exp.setAmount(request.getAmount());
            exp.setUser(authentication.getName());
            exp = expenditureRepository.save(exp);
            return ResponseEntity.ok(exp);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpenditure(@PathVariable String id,
                                               @RequestBody ExpenditureDTO dto,
                                               Authentication authentication) {

        Optional<Expenditure> existing = expenditureRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Does not exist");
        }

        Expenditure expenditure = existing.get();

        if (!expenditure.getUser().equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        expenditure.setTitle(dto.getTitle());
        expenditure.setAmount(dto.getAmount());

        return ResponseEntity.ok(expenditureRepository.save(expenditure));
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

