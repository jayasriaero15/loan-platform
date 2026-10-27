package com.bank.loan;

import static org.junit.jupiter.api.Assertions.*;

import com.bank.loan.dto.CustomerDto;
import com.bank.loan.service.EligibilityService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class EligibilityServiceTest {
    private final EligibilityService svc = new EligibilityService(650, 0.5, 20, 0.12);

    private CustomerDto customer(int score, long income) {
        return new CustomerDto(1L, "Test", "t@bank.com", "9999999999", BigDecimal.valueOf(income), score);
    }

    @Test
    void approvesGoodCustomer() {
        assertTrue(svc.evaluate(customer(750, 100000), BigDecimal.valueOf(500000), 36, true).approved());
    }

    @Test
    void rejectsWithoutKyc() {
        assertFalse(svc.evaluate(customer(750, 100000), BigDecimal.valueOf(500000), 36, false).approved());
    }

    @Test
    void rejectsLowCreditScore() {
        assertFalse(svc.evaluate(customer(600, 100000), BigDecimal.valueOf(500000), 36, true).approved());
    }

    @Test
    void rejectsWhenEmiTooHigh() {
        assertFalse(svc.evaluate(customer(750, 100000), BigDecimal.valueOf(1900000), 12, true).approved());
    }

    @Test
    void emiIsCalculated() {
        assertEquals(new BigDecimal("18888.89"), svc.emi(BigDecimal.valueOf(500000), 36));
    }
}
