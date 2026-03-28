package com.pranay.ecommerce_backend.entity;

public enum PaymentStatus {
    SUCCEEDED("succeeded"),
    PROCESSING("processing"),
    REQUIRES_CAPTURE("requires_capture");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentStatus fromString(String status) {
        for (PaymentStatus ps : PaymentStatus.values()) {
            if (ps.getValue().equalsIgnoreCase(status)) {
                return ps;
            }
        }
        throw new IllegalArgumentException("Unknown payment status: " + status);
    }
}