package com.bank.document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final KycDocumentRepository repo;
    private final String storageDir;
    private final long simulatedDelayMs;

    public DocumentController(KycDocumentRepository repo,
                              @Value("${document.storage-dir}") String storageDir,
                              @Value("${document.simulated-delay-ms:0}") long simulatedDelayMs) {
        this.repo = repo;
        this.storageDir = storageDir;
        this.simulatedDelayMs = simulatedDelayMs;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public KycDocument upload(@RequestParam Long customerId,
                              @RequestParam String docType,
                              @RequestParam("file") MultipartFile file) throws IOException {
        String original = file.getOriginalFilename() == null ? "upload.bin" : file.getOriginalFilename();
        String safeName = UUID.randomUUID() + "-" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path dir = Paths.get(storageDir, String.valueOf(customerId));
        Files.createDirectories(dir);
        Path target = dir.resolve(safeName);
        file.transferTo(target);

        KycDocument d = new KycDocument();
        d.setCustomerId(customerId);
        d.setDocType(docType);
        d.setFileName(original);
        d.setStoragePath(target.toString());
        d.setSizeBytes(file.getSize());
        return repo.save(d);
    }

    @GetMapping("/customer/{customerId}")
    public List<KycDocument> byCustomer(@PathVariable Long customerId) throws InterruptedException {
        if (simulatedDelayMs > 0) {
            Thread.sleep(simulatedDelayMs);   // demo knob: makes Document Service the trace bottleneck
        }
        return repo.findByCustomerId(customerId);
    }
}
