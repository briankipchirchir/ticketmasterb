package com.ticketmaster.controller;

import com.ticketmaster.model.ProofOfPayment;
import com.ticketmaster.service.ProofService;
import com.ticketmaster.service.SupabaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ticketmaster.service.EmailService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProofController {

    private final ProofService service;
    private final SupabaseService supabaseService;
    private final EmailService emailService;


    public ProofController(ProofService service, SupabaseService supabaseService) {
        this.service = service;
        this.supabaseService = supabaseService;
        this.emailService = emailService;
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
            @RequestParam("eventName") String eventName,
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
        System.out.println("SupabaseService is null? " + (supabaseService == null));
        System.out.println("========================================");

        try {
            System.out.println("Calling supabaseService.uploadFile()...");
            String fileUrl = supabaseService.uploadFile(file);
            System.out.println("Upload completed. File URL: " + fileUrl);
            System.out.println("fileUrl returned by Supabase: " + fileUrl);


            ProofOfPayment proof = new ProofOfPayment(
                    name, email, tickets, amount,
                    file.getOriginalFilename(),
                    fileUrl,
                    paymentMethod,
                    eventName
            );

            ProofOfPayment saved = service.saveProof(proof);
            System.out.println("Proof saved to database with ID: " + saved.getId());

            return ResponseEntity.ok(saved);

        } catch (IOException e) {
            System.err.println("========================================");
            System.err.println("ERROR: IOException caught");
            System.err.println("Message: " + e.getMessage());
            System.err.println("========================================");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("ERROR: Unexpected exception");
            System.err.println("Type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            System.err.println("========================================");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
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

    @PostMapping("/test-upload")
    public ResponseEntity<?> testUpload(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Testing Supabase upload...");
            String url = supabaseService.uploadFile(file);
            return ResponseEntity.ok("Success! URL: " + url);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/proofs/{id}/approve")
    public ResponseEntity<?> approveProof(@PathVariable Long id) {
        ProofOfPayment proof = service.getProofById(id);
        if (proof == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proof not found");
        }

        proof.setApproved(true);
        service.saveProof(proof);

        try {
            emailService.sendTicketEmail(  // ← NOW PROPERLY CALLED
                    proof.getUserEmail(),
                    proof.getUserName(),
                    proof.getTickets(),
                    proof.getEventName(),
                    proof.getAmount()
            );
            return ResponseEntity.ok("Approved and ticket sent to " + proof.getUserEmail());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Approved but email failed: " + e.getMessage());
        }
    }
}