package com.compliancevault.model;

import java.time.LocalDate;

public class PoliceCheck extends ComplianceDocument{
    private String checkProvider;
    private LocalDate issueDate;

    public PoliceCheck(int documentId, LocalDate startDate, LocalDate expiryDate,
                                String status, String checkProvider, LocalDate issueDate) {
        super(documentId, startDate, expiryDate, status);
        this.checkProvider = checkProvider;
        this.issueDate = issueDate;
    }

    @Override
    public String getAccessConsequence(){
        return "Blocked";
    }
}
