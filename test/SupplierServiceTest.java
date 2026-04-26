import com.compliancevault.dao.SupplierDAO;
import com.compliancevault.model.Supplier;
import com.compliancevault.service.SupplierService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SupplierServiceTest {

    private SupplierService service;
    private FakeSupplierDAO fakeDAO;

    @BeforeEach
    void setUp() {
        fakeDAO = new FakeSupplierDAO();
        service = new SupplierService(fakeDAO);

        // seed test data
        fakeDAO.suppliers.add(new Supplier(1, "MeathCo", "Jane Doe", "jane@meath.co",
                "0412345678", "123 Bobs St", "Hospitality", ""));
        fakeDAO.suppliers.add(new Supplier(2, "CleanPro Services", "Bob Smith", "bob@cleanpro.com",
                "0498765432", "45 Other St", "Cleaning", ""));
        fakeDAO.suppliers.add(new Supplier(3, "AcmePlumbing", "Sally Plumber", "sally@acme.com",
                "0411111111", "99 Pipe Rd", "Plumbing", ""));
    }

    @Test
    @DisplayName("exact Match Returns Result")
    void exactMatchReturnsResult() throws SQLException {
        var results = service.searchSuppliers("MeathCo");
        assertEquals(1, results.size());
        assertEquals("MeathCo", results.get(0).getCompanyName());
    }

    @Test
    @DisplayName("one Character Typo Still Matches")
    void oneCharacterTypoStillMatches() throws SQLException {
        // "meatco" is 1 edit away from "meathco"
        var results = service.searchSuppliers("meatco");
        assertFalse(results.isEmpty());
        assertEquals("MeathCo", results.get(0).getCompanyName());
    }

    @Test
    @DisplayName("wildly Different String Returns No Matches")
    void wildlyDifferentStringReturnsNoMatches() throws SQLException {
        // "xyz" is too far from any field
        var results = service.searchSuppliers("xyzqwerty");
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("empty Query Returns All Suppliers")
    void emptyQueryReturnsAllSuppliers() throws SQLException {
        var results = service.searchSuppliers("");
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("blank Query Returns All Suppliers")
    void blankQueryReturnsAllSuppliers() throws SQLException {
        var results = service.searchSuppliers("   ");
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("closer Match Ranks First")
    void closerMatchRanksFirst() throws SQLException {
        // "Meath" is exact substring in MeathCo (score 0)
        // No fuzzy match in CleanPro or AcmePlumbing
        var results = service.searchSuppliers("Meath");
        assertEquals("MeathCo", results.get(0).getCompanyName());
    }

    @Test
    @DisplayName("search Matches Phone Number")
    void searchMatchesPhoneNumber() throws SQLException {
        var results = service.searchSuppliers("0412345678");
        assertEquals(1, results.size());
        assertEquals("MeathCo", results.get(0).getCompanyName());
    }

    @Test
    @DisplayName("search Matches Service Type")
    void searchMatchesServiceType() throws SQLException {
        var results = service.searchSuppliers("Plumbing");
        assertEquals(1, results.size());
        assertEquals("AcmePlumbing", results.get(0).getCompanyName());
    }

    // in-memory fake DAO, extends real DAO but overrides the methods I use
    // means I don't hit the real database during tests
    private static class FakeSupplierDAO extends SupplierDAO {
        List<Supplier> suppliers = new ArrayList<>();

        @Override
        public List<Supplier> findAll() {
            return suppliers;
        }
    }
}