import com.compliancevault.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ComplianceServiceTest {

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        // fresh supplier with no docs before every test — ensures test isolation
        supplier = new Supplier(1, "TestCo", "Tester", "test@test.com",
                "0400000000", "Test Address", "Testing", "");
    }

    @Test
    @DisplayName("supplier With No Documents Is Non-Compliant")
    void supplierWithNoDocumentsIsNonCompliant() {
        // default-deny principle: empty document list = not compliant
        assertFalse(supplier.isCompliant());
    }

    @Test
    @DisplayName("supplier With All Valid Documents Is Compliant")
    void supplierWithAllValidDocumentsIsCompliant() {
        supplier.getComplianceDocuments().add(validInsurance());
        supplier.getComplianceDocuments().add(validPoliceCheck());
        supplier.getComplianceDocuments().add(validDeclaration());

        assertTrue(supplier.isCompliant());
    }

    @Test
    @DisplayName("expired Insurance Makes Supplier Non-Compliant")
    void expiredInsuranceMakesSupplierNonCompliant() {
        supplier.getComplianceDocuments().add(expiredInsurance());
        supplier.getComplianceDocuments().add(validPoliceCheck());

        assertFalse(supplier.isCompliant());
    }

    @Test
    @DisplayName("expired Police Check Makes Supplier Non-Compliant")
    void expiredPoliceCheckMakesSupplierNonCompliant() {
        supplier.getComplianceDocuments().add(validInsurance());
        supplier.getComplianceDocuments().add(expiredPoliceCheck());

        assertFalse(supplier.isCompliant());
    }

    @Test
    @DisplayName("non-Compliant Reasons Lists Expired Documents")
    void nonCompliantReasonsListsExpiredDocuments() {
        supplier.getComplianceDocuments().add(expiredInsurance());
        supplier.getComplianceDocuments().add(validPoliceCheck());

        var reasons = supplier.getNonCompliantReasons();
        assertEquals(1, reasons.size());
        assertTrue(reasons.get(0).contains("InsuranceCertificate"));
        assertTrue(reasons.get(0).contains("Blocked"));
    }

    @Test
    @DisplayName("compliant Supplier Has No Reasons")
    void compliantSupplierHasNoReasons() {
        supplier.getComplianceDocuments().add(validInsurance());
        supplier.getComplianceDocuments().add(validPoliceCheck());

        assertTrue(supplier.getNonCompliantReasons().isEmpty());
    }

    @Test
    @DisplayName("multiple Expired Documents All Appear In Reasons")
    void multipleExpiredDocumentsAllAppearInReasons() {
        supplier.getComplianceDocuments().add(expiredInsurance());
        supplier.getComplianceDocuments().add(expiredPoliceCheck());

        assertEquals(2, supplier.getNonCompliantReasons().size());
    }

    @Test
    @DisplayName("document Expiring Today Is Not Yet Expired")
    void documentExpiringTodayIsNotYetExpired() {
        // boundary case... expiry is "after today", so today itself is still valid
        InsuranceCertificate todayExpiry = new InsuranceCertificate(1,
                LocalDate.now().minusYears(1), LocalDate.now(),
                "Active", "Test", "POL-1", "CERT-1");
        assertFalse(todayExpiry.isExpired());
    }

    @Test
    @DisplayName("supervised Access Consequence For Agreement")
    void supervisedAccessConsequenceForAgreement() {
        // verifies polymorphism! Each subclass returns its own consequence
        SupplierAgreement agreement = new SupplierAgreement(1,
                LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(11),
                "Active", "general");
        assertEquals("Supervised access only", agreement.getAccessConsequence());
    }

    @Test
    @DisplayName("vaccination Declaration Does Not Restrict Access")
    void vaccinationDeclarationDoesNotRestrictAccess() {
        VaccinationDeclaration vax = new VaccinationDeclaration(1,
                LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(11),
                "Active", "Flu", true);
        assertEquals("No restriction", vax.getAccessConsequence());
    }

    // helper methods to keep tests readable and reduce duplication
    private InsuranceCertificate validInsurance() {
        return new InsuranceCertificate(1,
                LocalDate.now().minusMonths(6), LocalDate.now().plusDays(60),
                "Active", "Allianz", "POL-123", "CERT-001");
    }

    private InsuranceCertificate expiredInsurance() {
        return new InsuranceCertificate(2,
                LocalDate.now().minusYears(2), LocalDate.now().minusDays(30),
                "Expired", "Allianz", "POL-123", "CERT-001");
    }

    private PoliceCheck validPoliceCheck() {
        return new PoliceCheck(3,
                LocalDate.now().minusYears(1), LocalDate.now().plusYears(2),
                "Active", "WA Police", LocalDate.now().minusYears(1));
    }

    private PoliceCheck expiredPoliceCheck() {
        return new PoliceCheck(4,
                LocalDate.now().minusYears(4), LocalDate.now().minusDays(10),
                "Expired", "WA Police", LocalDate.now().minusYears(4));
    }

    private DeclarationForm validDeclaration() {
        return new DeclarationForm(5,
                LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6),
                "Active", "Jane Doe", LocalDate.now().minusMonths(6));
    }
}