package com.bank.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Simulated Email/SMS sender: logs the message. Replace with SendGrid/Twilio/SES in real life. */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    public record NotificationRequest(@NotBlank String to, @NotBlank String channel,
                                      String subject, @NotBlank String message) { }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> send(@Valid @RequestBody NotificationRequest r) {
        log.info("[{}] to={} subject='{}' message='{}'", r.channel(), r.to(), r.subject(), r.message());
        return Map.of("status", "SENT", "channel", r.channel(), "to", r.to());
    }
}
