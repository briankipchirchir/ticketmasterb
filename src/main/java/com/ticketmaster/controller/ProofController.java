package com.ticketmaster.controller;

import com.ticketmaster.model.ProofOfPayment;
import com.ticketmaster.service.ProofService;
import com.ticketmaster.service.SupabaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProofController {

    private final ProofService service;
    private final SupabaseService supabaseService;

    public ProofController(ProofService service, SupabaseService supabaseService) {
        this.service = service;
        this.supabaseService = supabaseService;
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
            // Upload to Supabase
            String fileUrl = supabaseService.uploadFile(file);

            ProofOfPayment proof = new ProofOfPayment(
                    name, email, tickets, amount,
                    file.getOriginalFilename(),
                    fileUrl, // Supabase public URL
                    paymentMethod
            );

            return ResponseEntity.ok(service.saveProof(proof));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file: " + e.getMessage());
        }
    }

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

        // Delete from Supabase
        try {
            supabaseService.deleteFile(proof.getFilePath());
        } catch (Exception e) {
            System.err.println("Failed to delete from Supabase: " + e.getMessage());
        }

        // Delete DB record
        service.deleteProof(id);

        return ResponseEntity.ok("Proof deleted successfully");
    }
}