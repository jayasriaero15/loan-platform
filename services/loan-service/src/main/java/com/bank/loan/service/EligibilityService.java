package com.bank.loan.service;

import com.bank.loan.dto.CustomerDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Loan eligibility rules. This is the logic a developer "updates" to trigger the CI/CD flow.
 * Thresholds are config-driven so v2 (canary) can run stricter/looser rules without code changes.
 */
@Service
public class EligibilityService {

    public record Decision(boolean approved, String reason) {
        static Decision approve() { return new Decision(true, "Meets all eligibility criteria"); }
        static Decision reject(String why) { return new Decision(false, why); }
    }

    private final int minCreditScore;
    private final double maxEmiRatio;
    private final double maxIncomeMultiple;
    private final double annualRate;

    public EligibilityService(@Value("${eligibility.min-credit-score:650}") int minCreditScore,
                              @Value("${eligibility.max-emi-ratio:0.5}") double maxEmiRatio,
                              @Value("${eligibility.max-income-multiple:20}") double maxIncomeMultiple,
                              @Value("${eligibility.annual-rate:0.12}") double annualRate) {
        this.minCreditScore = minCreditScore;
        this.maxEmiRatio = maxEmiRatio;
        this.maxIncomeMultiple = maxIncomeMultiple;
        this.annualRate = annualRate;
    }

    public Decision evaluate(CustomerDto c, BigDecimal amount, int tenureMonths, boolean kycUploaded) {
        if (!kycUploaded) {
            return Decision.reject("KYC documents not uploaded");
        }
        if (c.creditScore() < minCreditScore) {
            return Decision.reject("Credit score " + c.creditScore() + " below minimum " + minCreditScore);
        }
        BigDecimal income = c.monthlyIncome();
        BigDecimal emi = emi(amount, tenureMonths);
        if (emi.compareTo(income.multiply(BigDecimal.valueOf(maxEmiRatio))) > 0) {
            return Decision.reject("EMI " + emi + " exceeds " + (int) (maxEmiRatio * 100) + "% of monthly income");
        }
        if (amount.compareTo(income.multiply(BigDecimal.valueOf(maxIncomeMultiple))) > 0) {
            return Decision.reject("Amount exceeds " + (int) maxIncomeMultiple + "x monthly income");
        }
        return Decision.approve();
    }

    /** Flat-rate EMI (simplified for the demo). */
    public BigDecimal emi(BigDecimal amount, int months) {
        BigDecimal interest = amount.multiply(BigDecimal.valueOf(annualRate))
                .multiply(BigDecimal.valueOf(months))
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        return amount.add(interest).divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
    }
}
