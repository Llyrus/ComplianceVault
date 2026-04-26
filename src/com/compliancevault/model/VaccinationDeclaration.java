package com.compliancevault.model;

import java.time.LocalDate;

public class VaccinationDeclaration extends ComplianceDocument {
    private String vaccinationType;
    private boolean isOptional;

    public VaccinationDeclaration(int documentId, LocalDate startDate, LocalDate expiryDate,
                                  String status, String vaccinationType, boolean isOptional) {
        super(documentId, startDate, expiryDate, status);
        this.vaccinationType = vaccinationType;
        this.isOptional = isOptional;
    }

    @Override
    public String getAccessConsequence(){
        return "No restriction";
    }

    public String getVaccinationType() { return vaccinationType; }
    public boolean isOptional() { return isOptional; }
}
