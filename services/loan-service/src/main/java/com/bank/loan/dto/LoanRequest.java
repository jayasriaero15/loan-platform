package com.bank.loan.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record LoanRequest(
        @NotNull Long customerId,
        @NotNull @DecimalMin("1000") BigDecimal amount,
        @Min(6) @Max(84) int tenureMonths,
        String purpose) {
}
