package com.a3m.billing.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoiceData {

    // Bill info
    private int billId;
    private LocalDate billingDate;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    // Customer info
    private String customerName;
    private String customerAddress;
    private String msisdn;

    // Plan info
    private String planName;
    private BigDecimal monthlyFee;

    // Usage summary
    private int voiceUsageSeconds;
    private int dataUsageMB;
    private int smsUsageCount;

    // Charges
    private BigDecimal usageCost;
    private BigDecimal recurringFees;
    private BigDecimal oneTimeFees;
    private BigDecimal subtotal;
    private BigDecimal taxes;
    private BigDecimal totalAmount;

    // Package details
    private List<PackageDetail> packages;

    // One-time fee details
    private List<OneTimeFeeDetail> oneTimeFeeDetails;

    // --- Inner class: Package detail ---
    public static class PackageDetail {
        private String name;
        private String type;
        private int priority;
        private BigDecimal totalAmount;
        private BigDecimal consumed;
        private BigDecimal remaining;

        public PackageDetail(String name, String type, int priority,
                             BigDecimal totalAmount, BigDecimal consumed, BigDecimal remaining) {
            this.name = name;
            this.type = type;
            this.priority = priority;
            this.totalAmount = totalAmount;
            this.consumed = consumed;
            this.remaining = remaining;
        }

        public String getName()          { return name; }
        public String getType()          { return type; }
        public int getPriority()         { return priority; }
        public BigDecimal getTotalAmount(){ return totalAmount; }
        public BigDecimal getConsumed()  { return consumed; }
        public BigDecimal getRemaining() { return remaining; }

        public String getUnit() {
            switch (type) {
                case "voice": return "min";
                case "sms":   return "msgs";
                case "data":  return "MB";
                default:      return "";
            }
        }
    }

    // --- Inner class: One-time fee detail ---
    public static class OneTimeFeeDetail {
        private String name;
        private BigDecimal price;

        public OneTimeFeeDetail(String name, BigDecimal price) {
            this.name = name;
            this.price = price;
        }

        public String getName()      { return name; }
        public BigDecimal getPrice() { return price; }
    }

    // --- Getters and Setters ---
    public int getBillId()                          { return billId; }
    public void setBillId(int billId)               { this.billId = billId; }

    public LocalDate getBillingDate()               { return billingDate; }
    public void setBillingDate(LocalDate d)         { this.billingDate = d; }

    public LocalDate getPeriodStart()               { return periodStart; }
    public void setPeriodStart(LocalDate d)         { this.periodStart = d; }

    public LocalDate getPeriodEnd()                 { return periodEnd; }
    public void setPeriodEnd(LocalDate d)           { this.periodEnd = d; }

    public String getCustomerName()                 { return customerName; }
    public void setCustomerName(String s)           { this.customerName = s; }

    public String getCustomerAddress()              { return customerAddress; }
    public void setCustomerAddress(String s)        { this.customerAddress = s; }

    public String getMsisdn()                       { return msisdn; }
    public void setMsisdn(String s)                 { this.msisdn = s; }

    public String getPlanName()                     { return planName; }
    public void setPlanName(String s)               { this.planName = s; }

    public BigDecimal getMonthlyFee()               { return monthlyFee; }
    public void setMonthlyFee(BigDecimal v)         { this.monthlyFee = v; }

    public int getVoiceUsageSeconds()               { return voiceUsageSeconds; }
    public void setVoiceUsageSeconds(int v)         { this.voiceUsageSeconds = v; }

    public int getDataUsageMB()                     { return dataUsageMB; }
    public void setDataUsageMB(int v)               { this.dataUsageMB = v; }

    public int getSmsUsageCount()                   { return smsUsageCount; }
    public void setSmsUsageCount(int v)             { this.smsUsageCount = v; }

    public BigDecimal getUsageCost()                { return usageCost; }
    public void setUsageCost(BigDecimal v)          { this.usageCost = v; }

    public BigDecimal getRecurringFees()            { return recurringFees; }
    public void setRecurringFees(BigDecimal v)      { this.recurringFees = v; }

    public BigDecimal getOneTimeFees()              { return oneTimeFees; }
    public void setOneTimeFees(BigDecimal v)        { this.oneTimeFees = v; }

    public BigDecimal getSubtotal()                 { return subtotal; }
    public void setSubtotal(BigDecimal v)           { this.subtotal = v; }

    public BigDecimal getTaxes()                    { return taxes; }
    public void setTaxes(BigDecimal v)              { this.taxes = v; }

    public BigDecimal getTotalAmount()              { return totalAmount; }
    public void setTotalAmount(BigDecimal v)        { this.totalAmount = v; }

    public List<PackageDetail> getPackages()        { return packages; }
    public void setPackages(List<PackageDetail> v)  { this.packages = v; }

    public List<OneTimeFeeDetail> getOneTimeFeeDetails()       { return oneTimeFeeDetails; }
    public void setOneTimeFeeDetails(List<OneTimeFeeDetail> v) { this.oneTimeFeeDetails = v; }
}