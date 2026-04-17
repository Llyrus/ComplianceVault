package com.compliancevault.service;

import com.compliancevault.dao.ComplianceDocumentDAO;
import com.compliancevault.dao.SupplierDAO;
import com.compliancevault.model.ComplianceDocument;
import com.compliancevault.model.Supplier;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ComplianceService {
    private final SupplierDAO supplierDAO;
    private final ComplianceDocumentDAO documentDAO;

    public ComplianceService() {
        this.supplierDAO = new SupplierDAO();
        this.documentDAO = new ComplianceDocumentDAO();

    }


    //    Loads a supplier with all its compliance documents attached.
    //The DAOs return them separately, so we stitch them together here.
    public Supplier loadSupplierWithDocuments(int supplierId) throws SQLException {
        Supplier supplier = supplierDAO.findById(supplierId);
        if (supplier == null) return null;

        List<ComplianceDocument> docs = documentDAO.findBySupplierId(supplierId);
        for (ComplianceDocument doc : docs) {
            supplier.getComplianceDocuments().add(doc);
        }
        return supplier;
    }

    //Default-deny: returns false if supplier doesn't exist or has no documents
    public boolean isCompliant(int supplierId) throws SQLException {
        Supplier supplier = loadSupplierWithDocuments(supplierId);
        if (supplier == null) return false;
        return supplier.isCompliant();
    }

    public List<String> getNonCompliantReasons(int supplierId) throws SQLException {
        Supplier supplier = loadSupplierWithDocuments(supplierId);
        if (supplier == null) return List.of("Supplier not found");
        return supplier.getNonCompliantReasons();
    }


    //Returns documents across all suppliers that expire within the given threshold.
    //Used by NotificationService for the 30/15-day internal alerts and 7/1-day supplier alerts
    public List<ExpiringDocument> findDocumentsExpiringWithin(int days) throws SQLException {
        List<ExpiringDocument> expiring = new ArrayList<>();
        List<Supplier> allSuppliers = supplierDAO.findAll();

        for (Supplier supplier : allSuppliers) {
            List<ComplianceDocument> docs = documentDAO.findBySupplierId(supplier.getSupplierId());
            for (ComplianceDocument doc : docs) {
                int daysLeft = doc.daysUntilExpiry();
                if (daysLeft > 0 && daysLeft <= days) {
                    expiring.add(new ExpiringDocument(supplier, doc, daysLeft));
                }
            }
        }
        return expiring;
    }


    // Simple value class to bundle a document with its supplier and days remaining.
    //Saves the caller from having to look up the supplier separately
    public record ExpiringDocument(Supplier supplier, ComplianceDocument document, int daysUntilExpiry) {}

    // for NotificationService tests
    public ComplianceService(SupplierDAO supplierDAO, ComplianceDocumentDAO documentDAO) {
        this.supplierDAO = supplierDAO;
        this.documentDAO = documentDAO;
    }


}