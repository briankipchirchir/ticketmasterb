package com.ticketmaster.controller;

import com.ticketmaster.model.ProofOfPayment;
import com.ticketmaster.service.ProofService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // allow frontend requests
public class ProofController {

    private final ProofService service;

    // Absolute folder path for uploads (configured in application.properties)
    @Value("${ticket.upload.dir}")
    private String uploadDir;

    public ProofController(ProofService service) {
        this.service = service;
    }

    // POST /api/proof
    @PostMapping("/proof")
    public ResponseEntity<?> uploadProof(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("tickets") String tickets,
            @RequestParam("amount") Double amount,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // Ensure upload folder exists
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // Save file
            String filePath = "uploads/" + file.getOriginalFilename();

            File dest = new File(uploadDir, Objects.requireNonNull(file.getOriginalFilename()));
            file.transferTo(dest);


            // Save to DB
            ProofOfPayment proof = new ProofOfPayment(name, email,tickets,amount, file.getOriginalFilename(), filePath);
            ProofOfPayment saved = service.saveProof(proof);

            return ResponseEntity.ok(saved);

        } catch (MaxUploadSizeExceededException e) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body("File size exceeds maximum allowed!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save file: " + e.getMessage());
        }
    }

    // GET /api/proofs
    @GetMapping("/proofs")
    public List<ProofOfPayment> getAllProofs() {
        return service.getAllProofs();
    }
}
