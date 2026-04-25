package com.compliancevault.model;

import java.util.List;
import java.util.ArrayList;

public class Supplier {
    private int supplierId;
    private String companyName;
    private String contactName;
    private String email;
    private String phone;
    private String address;
    private String serviceType;
    private String notes;
    private List<ComplianceDocument> complianceDocuments;


    public Supplier(int supplierId, String companyName, String contactName, String email,
                    String phone, String address, String serviceType, String notes) {
        this.supplierId = supplierId;
        this.companyName = companyName;
        this.contactName = contactName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.serviceType = serviceType;
        this.notes = notes;
        this.complianceDocuments = new ArrayList<>();
    }

    // ASSIGNMENT NOTES:
    // perfect example of polymorphism, because .isExpired() has no idea what kind of doc its evaluating
    // Java figures it out at runtime!
    public boolean isCompliant() {
        //returns true if the supplier is compliant and false if not.
        // default deny, no docs means automatic block
        if (complianceDocuments.isEmpty()) {
            return false;
        }
        // must check if every compliance doc is current
        for (ComplianceDocument doc : complianceDocuments) {
            //check each doc, must get through the whole list with no blocks
            if (doc.isExpired()) {
                return false; //early stopping
            }
        }
        // if it made it this far, it's safe to return true!
        return true;
    }

    public List<String> getNonCompliantReasons() {
        // returns a list of reasons why the supplier is non-compliant to give to the end user
        List<String> reasons = new ArrayList<>();
        //loop through the docs
        for (ComplianceDocument doc : complianceDocuments) {
            //if doc is expired get the class name and append it to the array as a string
            if (doc.isExpired()) {
                String docName = doc.getClass().getSimpleName();
                // tell the end user why supplier is non-compliant and the consequence
                // so they don't have to go looking for it
                reasons.add(docName + " - expired " + doc.daysUntilExpiry()
                        + " days ago - " + doc.getAccessConsequence());
            }
        }
        return reasons;
    }


    //Getters for first test
    public List<ComplianceDocument> getComplianceDocuments() {
        return complianceDocuments;
    }
    public String getCompanyName() {
        return companyName;
    }

    // new getters and setter for DAO
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public String getContactName() { return contactName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getServiceType() { return serviceType; }
    public String getNotes() { return notes; }
}