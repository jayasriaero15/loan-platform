package com.bank.loan.web;

import com.bank.loan.dto.LoanRequest;
import com.bank.loan.model.LoanApplication;
import com.bank.loan.repo.LoanRepository;
import com.bank.loan.service.LoanService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    private final LoanService service;
    private final LoanRepository repo;

    public LoanController(LoanService service, LoanRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanApplication apply(@Valid @RequestBody LoanRequest request) {
        return service.apply(request);
    }

    @GetMapping("/{id}")
    public LoanApplication get(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
    }

    @GetMapping
    public List<LoanApplication> list(@RequestParam(required = false) Long customerId) {
        return customerId == null ? repo.findAll() : repo.findByCustomerId(customerId);
    }
}
