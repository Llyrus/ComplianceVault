package com.compliancevault.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public abstract class ComplianceDocument implements Expirable {
    private int documentId;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private String status;
    private Document attachedFile;

    public ComplianceDocument(int documentId, LocalDate startDate, LocalDate expiryDate, String status){
        this.documentId = documentId;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public boolean isExpired() {
        // A document is expired if today's date is after the expiry date...
        return LocalDate.now().isAfter(expiryDate);
    }

    public int daysUntilExpiry() {
        // calculate the days until the doc expires
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    // abstract method each subclass will override
    public abstract String getAccessConsequence();

    public int getDocumentId() { return documentId; }
    public void setDocumentId(int documentId) { this.documentId = documentId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getStatus() { return status; }
}

