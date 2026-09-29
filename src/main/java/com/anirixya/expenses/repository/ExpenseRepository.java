package com.anirixya.expenses.repository;

import com.anirixya.expenses.domain.Expense;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class ExpenseRepository {

    private final ConcurrentMap<UUID, Expense> storage = new ConcurrentHashMap<>();

    public Expense save(Expense expense) {
        storage.put(expense.getId(), expense);
        return expense;
    }

    public Optional<Expense> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Expense> findAll() {
        return new ArrayList<>(storage.values());
    }

    public boolean deleteById(UUID id) {
        return storage.remove(id) != null;
    }

    public boolean existsById(UUID id) {
        return storage.containsKey(id);
    }

    public void clear() {
        storage.clear();
    }
}
