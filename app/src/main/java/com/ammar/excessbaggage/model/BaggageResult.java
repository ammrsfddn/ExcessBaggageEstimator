package com.ammar.excessbaggage.model;

/** Holds the results of one calculation. */
public final class BaggageResult {

    private final double excessWeightKg;
    private final double ratePerKg;
    private final double excessCharge;
    private final double serviceTax;
    private final double totalPayable;

    public BaggageResult(double excessWeightKg, double ratePerKg,
                         double excessCharge, double serviceTax, double totalPayable) {
        this.excessWeightKg = excessWeightKg;
        this.ratePerKg = ratePerKg;
        this.excessCharge = excessCharge;
        this.serviceTax = serviceTax;
        this.totalPayable = totalPayable;
    }

    public double getExcessWeightKg() { return excessWeightKg; }
    public double getRatePerKg()      { return ratePerKg; }
    public double getExcessCharge()   { return excessCharge; }
    public double getServiceTax()     { return serviceTax; }
    public double getTotalPayable()   { return totalPayable; }
}