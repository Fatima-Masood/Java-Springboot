package com.expensetracker.expenditure;

import com.expensetracker.dto.ExpenditureDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExpenditureServiceTest {

    @Mock
    private ExpenditureRepository expenditureRepository;

    @InjectMocks
    private ExpenditureService expenditureService;

    private final String mockUser = "john";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //-------------------------
    // ADD EXPENDITURE
    //-------------------------
    @Test
    void testAddExpenditure_Success() {
        ExpenditureDTO dto = new ExpenditureDTO();
        dto.setTitle("Lunch");
        dto.setAmount(15.5);

        Expenditure savedExp = new Expenditure();
        savedExp.setId("1");
        savedExp.setTitle("Lunch");
        savedExp.setAmount(15.5);
        savedExp.setUser(mockUser);

        when(expenditureRepository.save(any(Expenditure.class))).thenReturn(savedExp);

        Expenditure result = expenditureService.addExpenditure(mockUser, dto);

        assertNotNull(result);
        assertEquals("Lunch", result.getTitle());
        assertEquals(15.5, result.getAmount());
        assertEquals(mockUser, result.getUser());

        ArgumentCaptor<Expenditure> captor = ArgumentCaptor.forClass(Expenditure.class);
        verify(expenditureRepository).save(captor.capture());
        assertEquals(mockUser, captor.getValue().getUser());
    }

    //-------------------------
    // UPDATE EXPENDITURE
    //-------------------------
    @Test
    void testUpdateExpenditure_Success() {
        Expenditure existing = new Expenditure();
        existing.setId("1");
        existing.setUser(mockUser);
        existing.setTitle("Old");
        existing.setAmount(10.0);

        ExpenditureDTO dto = new ExpenditureDTO();
        dto.setTitle("New");
        dto.setAmount(20.0);

        Expenditure updated = new Expenditure();
        updated.setId("1");
        updated.setUser(mockUser);
        updated.setTitle("New");
        updated.setAmount(20.0);

        when(expenditureRepository.findById("1")).thenReturn(Optional.of(existing));
        when(expenditureRepository.save(any(Expenditure.class))).thenReturn(updated);

        Optional<Expenditure> result = expenditureService.updateExpenditure(mockUser, "1", dto);

        assertTrue(result.isPresent());
        assertEquals("New", result.get().getTitle());
        assertEquals(20.0, result.get().getAmount());
    }

    @Test
    void testUpdateExpenditure_NotFound() {
        when(expenditureRepository.findById("999")).thenReturn(Optional.empty());

        Optional<Expenditure> result = expenditureService.updateExpenditure(mockUser, "999", new ExpenditureDTO());

        assertTrue(result.isEmpty());
        verify(expenditureRepository, never()).save(any());
    }

    @Test
    void testUpdateExpenditure_Forbidden() {
        Expenditure existing = new Expenditure();
        existing.setId("1");
        existing.setUser("other");

        when(expenditureRepository.findById("1")).thenReturn(Optional.of(existing));

        assertThrows(SecurityException.class, () ->
                expenditureService.updateExpenditure(mockUser, "1", new ExpenditureDTO())
        );
        verify(expenditureRepository, never()).save(any());
    }

    //-------------------------
    // DELETE EXPENDITURE
    //-------------------------
    @Test
    void testDeleteExpenditure_Success() {
        Expenditure existing = new Expenditure();
        existing.setId("1");
        existing.setUser(mockUser);

        when(expenditureRepository.findById("1")).thenReturn(Optional.of(existing));

        boolean result = expenditureService.deleteExpenditure(mockUser, "1");

        assertTrue(result);
        verify(expenditureRepository).deleteById("1");
    }

    @Test
    void testDeleteExpenditure_NotFound() {
        when(expenditureRepository.findById("999")).thenReturn(Optional.empty());

        boolean result = expenditureService.deleteExpenditure(mockUser, "999");

        assertFalse(result);
        verify(expenditureRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteExpenditure_Forbidden() {
        Expenditure existing = new Expenditure();
        existing.setId("1");
        existing.setUser("other");

        when(expenditureRepository.findById("1")).thenReturn(Optional.of(existing));

        assertThrows(SecurityException.class, () ->
                expenditureService.deleteExpenditure(mockUser, "1")
        );
        verify(expenditureRepository, never()).deleteById(any());
    }

    //-------------------------
    // GET EXPENDITURES
    //-------------------------
    @Test
    void testGetExpendituresByUser() {
        Expenditure e1 = new Expenditure();
        e1.setId("1");
        e1.setUser(mockUser);
        e1.setTitle("Coffee");
        e1.setAmount(5.0);

        when(expenditureRepository.findByUser(mockUser)).thenReturn(List.of(e1));

        List<Expenditure> results = expenditureService.getExpendituresByUser(mockUser);

        assertEquals(1, results.size());
        assertEquals("Coffee", results.get(0).getTitle());
    }
}
