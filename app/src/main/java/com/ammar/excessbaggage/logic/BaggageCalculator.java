package com.ammar.excessbaggage.logic;

import com.ammar.excessbaggage.model.BaggageResult;
import com.ammar.excessbaggage.model.TicketClass;

/** All the maths lives here (no Android code). */
public final class BaggageCalculator {

    /** 8% service tax. */
    public static final double SERVICE_TAX_RATE = 0.08;

    private BaggageCalculator() { }

    public static BaggageResult calculate(double weightKg,
                                          TicketClass ticketClass,
                                          double ratePerKg) {

        double excessWeight = Math.max(0.0, weightKg - ticketClass.getFreeAllowanceKg());
        double excessCharge = excessWeight * ratePerKg;
        double serviceTax   = excessCharge * SERVICE_TAX_RATE;
        double totalPayable = excessCharge + serviceTax;

        return new BaggageResult(excessWeightKg(excessWeight), ratePerKg,
                excessCharge, serviceTax, totalPayable);
    }

    private static double excessWeightKg(double value) {
        return value;
    }
}