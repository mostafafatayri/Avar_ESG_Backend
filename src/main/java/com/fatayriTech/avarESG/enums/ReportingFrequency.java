package com.fatayriTech.avarESG.enums;

public enum ReportingFrequency {
    MONTHLY(1),
    QUARTERLY(3),
    SEMI_ANNUAL(6),
    ANNUAL(12);

    private final int months;

    ReportingFrequency(int months) {
        this.months = months;
    }

    public int getMonths() {
        return months;
    }
}