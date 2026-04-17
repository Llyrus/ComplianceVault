import com.compliancevault.dao.ComplianceDocumentDAO;
import com.compliancevault.dao.SupplierDAO;
import com.compliancevault.model.*;
import com.compliancevault.service.ComplianceService;
import com.compliancevault.service.NotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationServiceTest {

    private NotificationService service;
    private FakeComplianceDocumentDAO fakeDocDAO;
    private FakeSupplierDAO fakeSupplierDAO;
    private Supplier testSupplier;

    @BeforeEach
    void setUp() {
        fakeSupplierDAO = new FakeSupplierDAO();
        fakeDocDAO = new FakeComplianceDocumentDAO();

        testSupplier = new Supplier(1, "TestCo", "Tester", "test@test.com",
                "0400000000", "Test Address", "Testing", "");
        fakeSupplierDAO.suppliers.add(testSupplier);

        ComplianceService complianceService = new ComplianceService(fakeSupplierDAO, fakeDocDAO);
        service = new NotificationService(complianceService);
    }

    @Test
    @DisplayName("document Expiring In Exactly 30 Days Triggers Internal Notification")
    void documentExpiringInExactly30DaysTriggersInternalNotification() throws SQLException {
        addDoc(insurance(LocalDate.now().plusDays(30)));

        var notifications = service.runDailyCheck();

        assertEquals(1, notifications.size());
        assertEquals(NotificationService.NotificationType.INTERNAL, notifications.get(0).type());
        assertEquals(30, notifications.get(0).daysUntilExpiry());
    }

    @Test
    @DisplayName("document Expiring In 29 Days Doesn't Trigger 30 Day Threshold")
    void documentExpiringIn29DaysDoesNotTrigger30DayThreshold() throws SQLException {
        // exact day matching prevents duplicate notifications
        addDoc(insurance(LocalDate.now().plusDays(29)));

        var notifications = service.runDailyCheck();

        // shouldn't fire 30-day, but might be in expiring list for other thresholds
        // none of 30/15/7/1 match exactly, so no notifications
        assertTrue(notifications.isEmpty());
    }

    @Test
    @DisplayName("Document expiring in 7 days trigger supplier email stub")
    void documentExpiringIn7DaysTriggersSupplierEmailStub() throws SQLException {
        addDoc(insurance(LocalDate.now().plusDays(7)));

        var notifications = service.runDailyCheck();

        assertEquals(1, notifications.size());
        assertEquals(NotificationService.NotificationType.SUPPLIER_EMAIL_STUB,
                notifications.get(0).type());
    }

    @Test
    @DisplayName("already Expired Document Does Not Trigger Notifications")
    void alreadyExpiredDocumentDoesNotTriggerNotifications() throws SQLException {
        // notification thresholds only fire BEFORE expiry, not after
        addDoc(insurance(LocalDate.now().minusDays(5)));

        var notifications = service.runDailyCheck();

        assertTrue(notifications.isEmpty());
    }

    @Test
    @DisplayName("notification Log Accumulates Across Multiple Runs")
    void notificationLogAccumulatesAcrossMultipleRuns() throws SQLException {
        addDoc(insurance(LocalDate.now().plusDays(30)));
        var firstRun = service.runDailyCheck();
        int afterFirst = service.getNotificationLog().size();

        addDoc(policeCheck(LocalDate.now().plusDays(15)));
        var secondRun = service.runDailyCheck();
        int afterSecond = service.getNotificationLog().size();

        // log grew between runs
        assertTrue(afterFirst > 0);
        assertTrue(afterSecond > afterFirst);
    }

    @Test
    @DisplayName("clear Log Empties The Notification Log")
    void clearLogEmptiesTheNotificationLog() throws SQLException {
        addDoc(insurance(LocalDate.now().plusDays(30)));
        service.runDailyCheck();

        assertFalse(service.getNotificationLog().isEmpty());

        service.clearLog();
        assertTrue(service.getNotificationLog().isEmpty());
    }

    // helpers
    private void addDoc(ComplianceDocument doc) {
        fakeDocDAO.docsBySupplier
                .computeIfAbsent(testSupplier.getSupplierId(), k -> new ArrayList<>())
                .add(doc);
    }

    private InsuranceCertificate insurance(LocalDate expiry) {
        return new InsuranceCertificate(1, LocalDate.now().minusMonths(6), expiry,
                "Active", "Allianz", "POL-1", "CERT-1");
    }

    private PoliceCheck policeCheck(LocalDate expiry) {
        return new PoliceCheck(2, LocalDate.now().minusYears(1), expiry,
                "Active", "WA Police", LocalDate.now().minusYears(1));
    }

    // fake DAOs
    private static class FakeSupplierDAO extends SupplierDAO {
        List<Supplier> suppliers = new ArrayList<>();
        @Override public List<Supplier> findAll() { return suppliers; }
    }

    private static class FakeComplianceDocumentDAO extends ComplianceDocumentDAO {
        Map<Integer, List<ComplianceDocument>> docsBySupplier = new HashMap<>();
        @Override public List<ComplianceDocument> findBySupplierId(int supplierId) {
            return docsBySupplier.getOrDefault(supplierId, new ArrayList<>());
        }
    }
}