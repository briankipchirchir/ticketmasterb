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

    // Constructors
    public ProofOfPayment() {}

    // Full constructor including tickets and amount
    public ProofOfPayment(String userName, String userEmail, String tickets, Double amount, String fileName, String filePath) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.tickets = tickets;
        this.amount = amount;
        this.fileName = fileName;
        this.filePath = filePath;
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
}
