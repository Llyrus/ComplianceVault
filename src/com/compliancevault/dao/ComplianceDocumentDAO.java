package com.compliancevault.dao;

import com.compliancevault.database.DatabaseManager;
import com.compliancevault.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ComplianceDocumentDAO {

    public void insert(int supplierId, ComplianceDocument doc) throws SQLException {
        String sql = """
            INSERT INTO compliance_documents (supplier_id, doc_type, start_date, expiry_date, status,
                insurer, policy_number, certificate_ref, check_provider, issue_date,
                signatory, date_signed, agreement_category, vaccination_type, is_optional)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, supplierId);
            pstmt.setString(2, doc.getClass().getSimpleName());
            pstmt.setString(3, doc.getStartDate().toString());
            pstmt.setString(4, doc.getExpiryDate().toString());
            pstmt.setString(5, doc.getStatus());

            // default all subclass fields to null
            for (int i = 6; i <= 15; i++) pstmt.setNull(i, Types.VARCHAR);

            // set subclass-specific fields
            if (doc instanceof InsuranceCertificate ins) {
                pstmt.setString(6, ins.getInsurer());
                pstmt.setString(7, ins.getPolicyNumber());
                pstmt.setString(8, ins.getCertificateRef());
            } else if (doc instanceof PoliceCheck pc) {
                pstmt.setString(9, pc.getCheckProvider());
                pstmt.setString(10, pc.getIssueDate().toString());
            } else if (doc instanceof DeclarationForm df) {
                pstmt.setString(11, df.getSignatory());
                pstmt.setString(12, df.getDateSigned().toString());
            } else if (doc instanceof SupplierAgreement sa) {
                pstmt.setString(13, sa.getAgreementCategory());
            } else if (doc instanceof VaccinationDeclaration vd) {
                pstmt.setString(14, vd.getVaccinationType());
                pstmt.setInt(15, vd.isOptional() ? 1 : 0);
            }

            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                doc.setDocumentId(keys.getInt(1));
            }
        }
    }


    public List<ComplianceDocument> findBySupplierId(int supplierId) throws SQLException {
        String sql = "SELECT * FROM compliance_documents WHERE supplier_id = ?";
        List<ComplianceDocument> docs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, supplierId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                docs.add(mapRow(rs));
            }
        }
        return docs;
    }

    public void delete(int documentId) throws SQLException {
        String sql = "DELETE FROM compliance_documents WHERE document_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, documentId);
            pstmt.executeUpdate();
        }
    }

    // factory pattern
    // reads the type column and creates the right subclass
    private ComplianceDocument mapRow(ResultSet rs) throws SQLException {
        String type = rs.getString("doc_type");
        int id = rs.getInt("document_id");
        LocalDate start = LocalDate.parse(rs.getString("start_date"));
        LocalDate expiry = LocalDate.parse(rs.getString("expiry_date"));
        String status = rs.getString("status");

        return switch (type) {
            case "InsuranceCertificate" -> new InsuranceCertificate(id, start, expiry, status,
                    rs.getString("insurer"), rs.getString("policy_number"), rs.getString("certificate_ref"));
            case "PoliceCheck" -> new PoliceCheck(id, start, expiry, status,
                    rs.getString("check_provider"), LocalDate.parse(rs.getString("issue_date")));
            case "DeclarationForm" -> new DeclarationForm(id, start, expiry, status,
                    rs.getString("signatory"), LocalDate.parse(rs.getString("date_signed")));
            case "SupplierAgreement" -> new SupplierAgreement(id, start, expiry, status,
                    rs.getString("agreement_category"));
            case "VaccinationDeclaration" -> new VaccinationDeclaration(id, start, expiry, status,
                    rs.getString("vaccination_type"), rs.getInt("is_optional") == 1);
            default -> throw new SQLException("Unknown document type: " + type);
        };
    }
}