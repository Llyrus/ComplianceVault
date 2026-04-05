package com.compliancevault.model;

public interface Expirable {
    boolean isExpired();
    int daysUntilExpiry();
}
