package com.bank.loan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerDto(Long id, String name, String email, String phone,
                          BigDecimal monthlyIncome, int creditScore) {
}
