package com.compliancevault.dao;

import com.compliancevault.database.DatabaseManager;
import com.compliancevault.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public void insert(Supplier supplier) throws SQLException {
        String sql = """
            INSERT INTO suppliers (company_name, contact_name, email, phone, address, service_type, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, supplier.getCompanyName());
            pstmt.setString(2, supplier.getContactName());
            pstmt.setString(3, supplier.getEmail());
            pstmt.setString(4, supplier.getPhone());
            pstmt.setString(5, supplier.getAddress());
            pstmt.setString(6, supplier.getServiceType());
            pstmt.setString(7, supplier.getNotes());
            pstmt.executeUpdate();

            // get the auto-generated ID and set it on the object
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                supplier.setSupplierId(keys.getInt(1));
            }
        }
    }

    public Supplier findById(int supplierId) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE supplier_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, supplierId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        }
    }

    public List<Supplier> findAll() throws SQLException {
        String sql = "SELECT * FROM suppliers";
        List<Supplier> suppliers = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                suppliers.add(mapRow(rs));
            }
        }
        return suppliers;
    }

    public void update(Supplier supplier) throws SQLException {
        String sql = """
            UPDATE suppliers SET company_name = ?, contact_name = ?, email = ?,
            phone = ?, address = ?, service_type = ?, notes = ?
            WHERE supplier_id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supplier.getCompanyName());
            pstmt.setString(2, supplier.getContactName());
            pstmt.setString(3, supplier.getEmail());
            pstmt.setString(4, supplier.getPhone());
            pstmt.setString(5, supplier.getAddress());
            pstmt.setString(6, supplier.getServiceType());
            pstmt.setString(7, supplier.getNotes());
            pstmt.setInt(8, supplier.getSupplierId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int supplierId) throws SQLException {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, supplierId);
            pstmt.executeUpdate();
        }
    }

    // helper method — turns a database row into a Supplier object
    private Supplier mapRow(ResultSet rs) throws SQLException {
        return new Supplier(
                rs.getInt("supplier_id"),
                rs.getString("company_name"),
                rs.getString("contact_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("service_type"),
                rs.getString("notes")
        );
    }
}