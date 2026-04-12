package com.compliancevault.model;

import java.time.LocalDate;

public class DeclarationForm extends ComplianceDocument{
    private String signatory;
    private LocalDate dateSigned;

    public DeclarationForm(int documentId, LocalDate startDate, LocalDate expiryDate,
                           String status, String signatory, LocalDate dateSigned) {
        super(documentId, startDate, expiryDate, status);
        this.signatory = signatory;
        this.dateSigned = dateSigned;
    }

    @Override
    public String getAccessConsequence(){
        return "Blocked";
    }

    public String getSignatory() { return signatory; }
    public LocalDate getDateSigned() { return dateSigned; }
}
