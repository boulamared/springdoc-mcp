package com.anirixya.expenses.service;

import com.anirixya.expenses.domain.Expense;
import com.anirixya.expenses.dto.ExpenseDto;
import com.anirixya.expenses.dto.ExpenseRecord;
import com.anirixya.expenses.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ExpenseApplicationService {

    private final ExpenseRepository expenseRepository;

    public ExpenseApplicationService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public ExpenseRecord submitExpense(ExpenseDto dto) {
        Expense expense = Expense.create(dto.amount(), dto.currency(), dto.category());
        Expense saved = expenseRepository.save(expense);
        return toRecord(saved);
    }

    public ExpenseRecord getExpense(UUID id) {
        return expenseRepository.findById(id)
            .map(this::toRecord)
            .orElseThrow(() -> new NoSuchElementException("Expense not found with ID: " + id));
    }

    public List<ExpenseRecord> listAllExpenses() {
        return expenseRepository.findAll().stream()
            .map(this::toRecord)
            .toList();
    }

    public void deleteExpense(UUID id) {
        if (!expenseRepository.deleteById(id)) {
            throw new NoSuchElementException("Expense not found with ID: " + id);
        }
    }

    private ExpenseRecord toRecord(Expense expense) {
        return new ExpenseRecord(
            expense.getId(),
            expense.getAmount(),
            expense.getCurrency(),
            expense.getCategory(),
            expense.getCreatedAt()
        );
    }
}
