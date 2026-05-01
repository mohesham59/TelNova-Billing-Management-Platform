/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.telecom.billing.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author Ali
 */
public class BillSummary {

    private int id;
    private LocalDate billingDate;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal recurringFees;
    private BigDecimal oneTimeFees;
    private BigDecimal taxes;
    private BigDecimal subtotal;
    private BigDecimal totalAmount;
    private int voiceUsage;   // seconds
    private int dataUsage;    // MB
    private int smsUsage;     // count

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(LocalDate d) {
        this.billingDate = d;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate d) {
        this.periodStart = d;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate d) {
        this.periodEnd = d;
    }

    public BigDecimal getRecurringFees() {
        return recurringFees;
    }

    public void setRecurringFees(BigDecimal v) {
        this.recurringFees = v;
    }

    public BigDecimal getOneTimeFees() {
        return oneTimeFees;
    }

    public void setOneTimeFees(BigDecimal v) {
        this.oneTimeFees = v;
    }

    public BigDecimal getTaxes() {
        return taxes;
    }

    public void setTaxes(BigDecimal v) {
        this.taxes = v;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal v) {
        this.subtotal = v;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal v) {
        this.totalAmount = v;
    }

    public int getVoiceUsage() {
        return voiceUsage;
    }

    public void setVoiceUsage(int v) {
        this.voiceUsage = v;
    }

    public int getDataUsage() {
        return dataUsage;
    }

    public void setDataUsage(int v) {
        this.dataUsage = v;
    }

    public int getSmsUsage() {
        return smsUsage;
    }

    public void setSmsUsage(int v) {
        this.smsUsage = v;
    }

    /**
     * Convert voice seconds to human-readable string.
     */
    public String getVoiceFormatted() {
        int h = voiceUsage / 3600, m = (voiceUsage % 3600) / 60, s = voiceUsage % 60;
        if (h > 0) {
            return h + "h " + m + "m";
        }
        if (m > 0) {
            return m + "m " + s + "s";
        }
        return s + "s";
    }
}
