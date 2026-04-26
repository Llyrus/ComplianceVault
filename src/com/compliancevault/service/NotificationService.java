package com.compliancevault.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final ComplianceService complianceService;

    // notification thresholds in days, per requirements section 6
    private static final int[] INTERNAL_THRESHOLDS = {30, 15};
    private static final int[] SUPPLIER_THRESHOLDS = {7, 1};

    // log of notifications generated, viewable in the UI for the uni version
    private final List<Notification> notificationLog;

    public NotificationService(ComplianceService complianceService) {
        this.complianceService = complianceService;
        this.notificationLog = new ArrayList<>();
    }


    //Run the daily compliance check. Per requirements, this fires at 2:00 PM
    //and generates notifications for documents approaching expiry.
    //Returns the notifications generated this run.
    public List<Notification> runDailyCheck() throws SQLException {
        List<Notification> generated = new ArrayList<>();

        // internal — quality admin officer notifications
        for (int threshold : INTERNAL_THRESHOLDS) {
            for (var expiring : complianceService.findDocumentsExpiringWithin(threshold)) {
                if (expiring.daysUntilExpiry() == threshold) {
                    Notification n = new Notification(
                            NotificationType.INTERNAL,
                            expiring.supplier().getCompanyName(),
                            expiring.supplier().getEmail(),
                            expiring.document().getClass().getSimpleName(),
                            expiring.daysUntilExpiry(),
                            LocalDateTime.now()
                    );
                    generated.add(n);
                    notificationLog.add(n);
                }
            }
        }

        // supplier stubbed email notifications
        // (logged, not sent via SMTP)
        for (int threshold : SUPPLIER_THRESHOLDS) {
            for (var expiring : complianceService.findDocumentsExpiringWithin(threshold)) {
                if (expiring.daysUntilExpiry() == threshold) {
                    Notification n = new Notification(
                            NotificationType.SUPPLIER_EMAIL_STUB,
                            expiring.supplier().getCompanyName(),
                            expiring.supplier().getEmail(),
                            expiring.document().getClass().getSimpleName(),
                            expiring.daysUntilExpiry(),
                            LocalDateTime.now()
                    );
                    generated.add(n);
                    notificationLog.add(n);
                    System.out.println("[EMAIL STUB] To: " + n.recipientEmail()
                            + " — " + n.documentType() + " expires in " + n.daysUntilExpiry() + " day(s)");
                }
            }
        }

        return generated;
    }

    public List<Notification> getNotificationLog() {
        return new ArrayList<>(notificationLog);
    }

    public void clearLog() {
        notificationLog.clear();
    }

    public enum NotificationType {
        INTERNAL,            // shown in-app to quality admin
        SUPPLIER_EMAIL_STUB  // would be SMTP in production
    }

    public record Notification(
            NotificationType type,
            String supplierName,
            String recipientEmail,
            String documentType,
            int daysUntilExpiry,
            LocalDateTime generatedAt
    ) {}
}