package com.ammar.excessbaggage.model;

/** The two ticket classes and their FREE baggage allowance (kg). */
public enum TicketClass {

    ECONOMY(20.0),
    BUSINESS(40.0);

    private final double freeAllowanceKg;

    TicketClass(double freeAllowanceKg) {
        this.freeAllowanceKg = freeAllowanceKg;
    }

    public double getFreeAllowanceKg() {
        return freeAllowanceKg;
    }
}