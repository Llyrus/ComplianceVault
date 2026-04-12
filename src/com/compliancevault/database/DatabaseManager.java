package com.compliancevault.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:compliancevault.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialiseDatabase() {
        String createSuppliersTable = """
                CREATE TABLE IF NOT EXISTS suppliers (
                    supplier_id    INTEGER PRIMARY KEY AUTOINCREMENT,
                    company_name   TEXT NOT NULL,
                    contact_name   TEXT,
                    email          TEXT,
                    phone          TEXT,
                    address        TEXT,
                    service_type   TEXT,
                    notes          TEXT
                );
                """;

        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    username       TEXT NOT NULL UNIQUE,
                    password_hash  TEXT NOT NULL,
                    role           TEXT NOT NULL
                );
                """;

        String createComplianceDocumentsTable = """
                CREATE TABLE IF NOT EXISTS compliance_documents (
                    document_id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    supplier_id        INTEGER NOT NULL,
                    doc_type           TEXT NOT NULL,
                    start_date         TEXT,
                    expiry_date        TEXT,
                    status             TEXT,
                    file_path          TEXT,
                    insurer            TEXT,
                    policy_number      TEXT,
                    certificate_ref    TEXT,
                    check_provider     TEXT,
                    issue_date         TEXT,
                    signatory          TEXT,
                    date_signed        TEXT,
                    agreement_category TEXT,
                    vaccination_type   TEXT,
                    is_optional        INTEGER,
                    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
                );
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createSuppliersTable);
            stmt.execute(createUsersTable);
            stmt.execute(createComplianceDocumentsTable);

            System.out.println("Database initialised successfully.");

        } catch (SQLException e) {
            System.out.println("Database initialisation failed: " + e.getMessage());
        }
    }
}