package com.ticketmaster.model;

import jakarta.persistence.*;

@Entity
public class ProofOfPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;
    private String userEmail;
    private String tickets; // Stores ticket selection
    private Double amount;   // Stores total amount paid

    private String fileName; // Stores the uploaded file name
    private String filePath; // Stores the local path where file is saved

    private String paymentMethod;
    private String eventName;        // ← new

    private java.time.LocalDateTime uploadedAt;  // ← new
    private boolean approved = false;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = java.time.LocalDateTime.now(); // ← auto-set on save
    }



    // Constructors
    public ProofOfPayment() {}

    // Full constructor including tickets and amount
    public ProofOfPayment(String userName, String userEmail, String tickets, Double amount, String fileName, String filePath, String paymentMethod,String eventName) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.tickets = tickets;
        this.amount = amount;
        this.fileName = fileName;
        this.filePath = filePath;
        this.paymentMethod = paymentMethod;
        this.eventName = eventName;
    }

    // Getters & Setters
    public Long getId() { return id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getTickets() { return tickets; }
    public void setTickets(String tickets) { this.tickets = tickets; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public java.time.LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(java.time.LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
}
