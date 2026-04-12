import com.compliancevault.database.DatabaseManager;
import com.compliancevault.dao.*;
import com.compliancevault.model.*;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        DatabaseManager.initialiseDatabase();

        try {
            // create and save a supplier
            SupplierDAO supplierDAO = new SupplierDAO();
            Supplier supplier = new Supplier(0, "MeathCo", "Jane Doe",
                    "jane@meath.co", "12345678", "123 Bobs St Perth, 6026",
                    "Hospitality", "Test supplier");
            supplierDAO.insert(supplier);
            System.out.println("Supplier saved with ID: " + supplier.getSupplierId());

            // save compliance documents against that supplier
            ComplianceDocumentDAO docDAO = new ComplianceDocumentDAO();

            InsuranceCertificate insurance = new InsuranceCertificate(0,
                    LocalDate.now().minusMonths(6), LocalDate.now().plusDays(60),
                    "Active", "Allianz", "POL-12345", "CERT-2026-001");
            docDAO.insert(supplier.getSupplierId(), insurance);

            PoliceCheck police = new PoliceCheck(0,
                    LocalDate.now().minusYears(1), LocalDate.now().plusYears(2),
                    "Active", "WA Police", LocalDate.now().minusYears(1));
            docDAO.insert(supplier.getSupplierId(), police);

            DeclarationForm declaration = new DeclarationForm(0,
                    LocalDate.now().minusYears(2), LocalDate.now().minusDays(30),
                    "Expired", "Jane Doe", LocalDate.now().minusYears(2));
            docDAO.insert(supplier.getSupplierId(), declaration);

            // read them back and check compliance
            var docs = docDAO.findBySupplierId(supplier.getSupplierId());
            for (ComplianceDocument doc : docs) {
                supplier.getComplianceDocuments().add(doc);
            }

            System.out.println("Documents loaded: " + docs.size());
            System.out.println("Compliant: " + supplier.isCompliant());
            System.out.println("Reasons: " + supplier.getNonCompliantReasons());

            // save a user
            UserDAO userDAO = new UserDAO();
            User admin = new User(0, "admin", "hashed_password_here", Role.ADMIN);
            userDAO.insert(admin);

            User loaded = userDAO.findByUsername("admin");
            System.out.println("User loaded: " + loaded.getUsername() + " - " + loaded.getRole());

            System.out.println("\n--- Phase 2 complete! ---");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}