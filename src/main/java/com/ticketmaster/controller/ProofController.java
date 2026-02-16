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
        System.out.println("========================================");
        System.out.println("ProofController INITIALIZED");
        System.out.println("SupabaseService injected: " + (supabaseService != null ? "YES" : "NO"));
        System.out.println("========================================");
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
        System.out.println("========================================");
        System.out.println("UPLOAD PROOF REQUEST RECEIVED");
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Tickets: " + tickets);
        System.out.println("Amount: " + amount);
        System.out.println("Payment Method: " + paymentMethod);
        System.out.println("File: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)");
        System.out.println("========================================");

        try {
            // Upload to Supabase
            System.out.println("Calling supabaseService.uploadFile()...");
            String fileUrl = supabaseService.uploadFile(file);
            System.out.println("Upload completed. File URL: " + fileUrl);

            ProofOfPayment proof = new ProofOfPayment(
                    name, email, tickets, amount,
                    file.getOriginalFilename(),
                    fileUrl,
                    paymentMethod
            );

            ProofOfPayment saved = service.saveProof(proof);
            System.out.println("Proof saved to database with ID: " + saved.getId());

            return ResponseEntity.ok(saved);

        } catch (IOException e) {
            System.err.println("ERROR: Failed to upload file");
            e.printStackTrace();
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

        try {
            supabaseService.deleteFile(proof.getFilePath());
        } catch (Exception e) {
            System.err.println("Failed to delete from Supabase: " + e.getMessage());
        }

        service.deleteProof(id);

        return ResponseEntity.ok("Proof deleted successfully");
    }
}