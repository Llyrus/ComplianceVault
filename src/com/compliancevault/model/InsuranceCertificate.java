package com.compliancevault.model;

import java.time.LocalDate;

public class InsuranceCertificate extends ComplianceDocument{
    private String insurer;
    private String policyNumber;
    private String certificateRef;

    // Constructor class building on the main abstract class but adding the three extra vars
    public InsuranceCertificate(int documentId, LocalDate startDate, LocalDate expiryDate,
                                String status, String insurer, String policyNumber,
                                String certificateRef) {
        super(documentId, startDate, expiryDate, status);
        this.insurer = insurer;
        this.policyNumber = policyNumber;
        this.certificateRef = certificateRef;
    }

    @Override
    public String getAccessConsequence(){
        return "Blocked";
    }

    public String getInsurer() { return insurer; }
    public String getPolicyNumber() { return policyNumber; }
    public String getCertificateRef() { return certificateRef; }
}
