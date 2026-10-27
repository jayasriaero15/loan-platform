package com.bank.loan.client;

import com.bank.loan.dto.CustomerDto;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

/** Plain HTTP calls to other services. Istio sidecars add mTLS/retries transparently. */
@Component
public class DownstreamClient {
    private static final Logger log = LoggerFactory.getLogger(DownstreamClient.class);

    private final RestTemplate rest;
    private final String customerUrl;
    private final String documentUrl;
    private final String notificationUrl;

    public DownstreamClient(RestTemplate rest,
                            @Value("${services.customer.url}") String customerUrl,
                            @Value("${services.document.url}") String documentUrl,
                            @Value("${services.notification.url}") String notificationUrl) {
        this.rest = rest;
        this.customerUrl = customerUrl;
        this.documentUrl = documentUrl;
        this.notificationUrl = notificationUrl;
    }

    public CustomerDto getCustomer(Long id) {
        try {
            return rest.getForObject(customerUrl + "/api/customers/{id}", CustomerDto.class, id);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer " + id + " not found");
        }
    }

    public boolean hasKycDocuments(Long customerId) {
        Object[] docs = rest.getForObject(documentUrl + "/api/documents/customer/{id}", Object[].class, customerId);
        return docs != null && docs.length > 0;
    }

    /** Notification failures must never fail the loan decision. */
    public void notifyCustomer(CustomerDto c, String subject, String message) {
        try {
            rest.postForObject(notificationUrl + "/api/notifications",
                    Map.of("to", c.email(), "channel", "EMAIL", "subject", subject, "message", message),
                    String.class);
        } catch (Exception e) {
            log.warn("Notification failed for customer {}: {}", c.id(), e.getMessage());
        }
    }
}
