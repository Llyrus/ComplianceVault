package com.compliancevault.model;

import java.time.LocalDate;

public class SupplierAgreement extends ComplianceDocument{
    private String agreementCategory;

    public SupplierAgreement(int documentId, LocalDate startDate, LocalDate expiryDate,
                             String status,String agreementCategory) {
        super(documentId, startDate, expiryDate, status);
        this.agreementCategory = agreementCategory;
    }
    @Override
    public String getAccessConsequence(){
        return "Supervised access only";
    }
}
