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
    @PostMapping("/proof")
    public ResponseEntity<?> uploadProof(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("tickets") String tickets,
            @RequestParam("amount") Double amount,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("file") MultipartFile file

    ) {
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new IOException("Could not create upload directory: " + uploadDir);
                }
            }


            String originalName = Objects.requireNonNull(file.getOriginalFilename());
            String uniqueName = System.currentTimeMillis() + "_" + originalName;

            File dest = new File(dir, uniqueName);
            file.transferTo(dest);

            String filePath = "uploads/" + uniqueName;

            ProofOfPayment proof = new ProofOfPayment(
                    name,
                    email,
                    tickets,
                    amount,
                    uniqueName,
                    filePath,
                    paymentMethod
            );

            return ResponseEntity.ok(service.saveProof(proof));

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


    @DeleteMapping("/proofs/{id}")
    public ResponseEntity<?> deleteProof(@PathVariable Long id) {
        ProofOfPayment proof = service.getProofById(id);

        if (proof == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Proof not found");
        }

        // Delete file from disk
        File file = new File(uploadDir, proof.getFileName());
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to delete file");
            }
        }

        // Delete DB record
        service.deleteProof(id);

        return ResponseEntity.ok("Proof deleted successfully");
    }

}
