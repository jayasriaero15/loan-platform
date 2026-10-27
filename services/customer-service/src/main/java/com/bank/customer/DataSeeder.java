package com.bank.customer;

import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Seeds demo customers on first start. */
@Component
public class DataSeeder implements CommandLineRunner {
    private final CustomerRepository repo;

    public DataSeeder(CustomerRepository repo) { this.repo = repo; }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        repo.save(new Customer("Asha Raman",  "asha@example.com",  "9000000001", BigDecimal.valueOf(120000), 780)); // strong
        repo.save(new Customer("Ravi Kumar",  "ravi@example.com",  "9000000002", BigDecimal.valueOf(45000),  610)); // low score
        repo.save(new Customer("Meena Iyer",  "meena@example.com", "9000000003", BigDecimal.valueOf(60000),  700)); // borderline
    }
}
