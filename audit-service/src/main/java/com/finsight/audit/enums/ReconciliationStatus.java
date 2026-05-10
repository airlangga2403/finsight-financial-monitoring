package com.finsight.audit.enums;

public enum ReconciliationStatus {
    BALANCED,           // All accounts reconcile — net flow matches reported balance changes
    DISCREPANCY_FOUND,  // One or more accounts have unexplained balance gaps
    PENDING             // Run triggered but not yet evaluated
}
