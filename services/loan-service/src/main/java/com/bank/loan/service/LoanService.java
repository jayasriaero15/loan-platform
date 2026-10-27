package com.bank.loan.service;

import com.bank.loan.client.DownstreamClient;
import com.bank.loan.dto.CustomerDto;
import com.bank.loan.dto.LoanRequest;
import com.bank.loan.model.LoanApplication;
import com.bank.loan.repo.LoanRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoanService {
    private final DownstreamClient clients;
    private final EligibilityService eligibility;
    private final LoanRepository repo;
    private final String engineVersion;

    public LoanService(DownstreamClient clients, EligibilityService eligibility, LoanRepository repo,
                       @Value("${app.version}") String engineVersion) {
        this.clients = clients;
        this.eligibility = eligibility;
        this.repo = repo;
        this.engineVersion = engineVersion;
    }

    public LoanApplication apply(LoanRequest req) {
        CustomerDto customer = clients.getCustomer(req.customerId());          // -> Customer Service
        boolean kyc = clients.hasKycDocuments(req.customerId());                // -> Document Service
        EligibilityService.Decision d =
                eligibility.evaluate(customer, req.amount(), req.tenureMonths(), kyc);

        LoanApplication app = new LoanApplication();
        app.setCustomerId(req.customerId());
        app.setAmount(req.amount());
        app.setTenureMonths(req.tenureMonths());
        app.setPurpose(req.purpose());
        app.setStatus(d.approved() ? "APPROVED" : "REJECTED");
        app.setReason(d.reason());
        app.setEngineVersion(engineVersion);
        app = repo.save(app);

        clients.notifyCustomer(customer,                                         // -> Notification Service
                "Loan application #" + app.getId() + " " + app.getStatus(),
                "Dear " + customer.name() + ", your loan application for " + app.getAmount()
                        + " is " + app.getStatus() + ". " + app.getReason());
        return app;
    }
}
