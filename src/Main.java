import com.compliancevault.model.DeclarationForm;
import com.compliancevault.model.InsuranceCertificate;
import com.compliancevault.model.PoliceCheck;
import com.compliancevault.model.Supplier;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        System.out.println(" First ComplianceVault Model Test ");

        // some data to test with
        //supplier details
        Supplier supplier = new Supplier(1, "MeathCo", "Jane Doe",
                                        "jane@meath.co", "12345678", "123 bobs st Perth, 6026",
                            "Hospitality", "Horrible cook");

        //insurance details
        InsuranceCertificate insurance = new InsuranceCertificate (1, LocalDate.now().minusMonths(6),
                    LocalDate.now().plusDays(60), "Active",
                "Allianz", "POL-12345", "CERT-2026-001");
        supplier.getComplianceDocuments().add(insurance);

        // police check
        PoliceCheck policeCheck = new PoliceCheck(2, LocalDate.now().minusYears(1),
                LocalDate.now().plusYears(2), "Active", "WA Police", LocalDate.now().minusYears(1));
        supplier.getComplianceDocuments().add(policeCheck);

        // check compliance logic
        System.out.println("Supplier: " + supplier.getCompanyName());
        System.out.println("Compliant: " + supplier.isCompliant());
        System.out.println("Reason: " + supplier.getNonCompliantReasons());

        System.out.println(" Add expired declaration\n");

        // add an expired doc to test
        DeclarationForm expiredDec = new DeclarationForm(3,
                LocalDate.now().minusYears(2),
                LocalDate.now().minusDays(30),
                "Expired", "Jane Doe", LocalDate.now().minusYears(2));
        supplier.getComplianceDocuments().add(expiredDec);

        System.out.println("Supplier: " + supplier.getCompanyName());
        System.out.println("Compliant: " + supplier.isCompliant());
        System.out.println("Reason: " + supplier.getNonCompliantReasons());

    }
}