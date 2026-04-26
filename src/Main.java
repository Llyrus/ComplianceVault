import com.compliancevault.database.DatabaseManager;
import com.compliancevault.model.*;
import com.compliancevault.service.*;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        DatabaseManager.initialiseDatabase();

        try {
            // === SERVICES ===
            SupplierService supplierService = new SupplierService();
            ComplianceService complianceService = new ComplianceService();
            AuthService authService = new AuthService();
            NotificationService notificationService = new NotificationService(complianceService);

            // === AUTH ===
            System.out.println("--- Auth ---");
            authService.registerUser("admin", "password123", Role.ADMIN);
            authService.registerUser("reception", "password456", Role.GENERAL);

            System.out.println("Login with wrong password: " + authService.login("admin", "wrong"));
            System.out.println("Login as admin: " + authService.login("admin", "password123"));
            System.out.println("Has admin access: " + authService.hasAdminAccess());

            // === SUPPLIER + DOCUMENTS ===
            System.out.println("\n--- Suppliers ---");
            Supplier s1 = new Supplier(0, "MeathCo", "Jane Doe", "jane@meath.co",
                    "0412345678", "123 Bobs St Perth", "Hospitality", "");
            supplierService.createSupplier(s1);

            // attach docs via DAO directly for now (a SupplierService.addDocument()
            // method would be the next refinement)
            var docDAO = new com.compliancevault.dao.ComplianceDocumentDAO();
            docDAO.insert(s1.getSupplierId(), new InsuranceCertificate(0,
                    LocalDate.now().minusMonths(6), LocalDate.now().plusDays(15),
                    "Active", "Allianz", "POL-123", "CERT-001"));
            docDAO.insert(s1.getSupplierId(), new PoliceCheck(0,
                    LocalDate.now().minusYears(1), LocalDate.now().plusDays(30),
                    "Active", "WA Police", LocalDate.now().minusYears(1)));

            System.out.println("Compliant: " + complianceService.isCompliant(s1.getSupplierId()));

            // === FUZZY SEARCH ===
            System.out.println("\n--- Fuzzy Search ---");
            System.out.println("Search 'meathco' (exact): " + supplierService.searchSuppliers("meathco").size());
            System.out.println("Search 'meatco' (typo): " + supplierService.searchSuppliers("meatco").size());
            System.out.println("Search 'hospitality': " + supplierService.searchSuppliers("hospitality").size());
            System.out.println("Search 'plumbing': " + supplierService.searchSuppliers("plumbing").size());

            // === NOTIFICATIONS ===
            System.out.println("\n--- Notifications ---");
            var notifs = notificationService.runDailyCheck();
            System.out.println("Notifications generated: " + notifs.size());
            for (var n : notifs) {
                System.out.println("  [" + n.type() + "] " + n.supplierName()
                        + " — " + n.documentType() + " expires in " + n.daysUntilExpiry() + " day(s)");
            }

            System.out.println("\n--- Phase 3 complete! ---");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}