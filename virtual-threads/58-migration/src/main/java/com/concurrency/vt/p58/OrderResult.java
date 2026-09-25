package com.concurrency.vt.p58;

/** Shared result type for both Legacy and Migrated services. */
public record OrderResult(
        String orderId,
        String customerId,
        double amount,
        boolean success) {}
