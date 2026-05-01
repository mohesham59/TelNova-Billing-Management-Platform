/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.telecom.billing.model;

import java.math.BigDecimal;

/**
 *
 * @author Ali
 */
public class RatePlan {

    private int id;
    private String planName;
    private BigDecimal rorData;
    private BigDecimal rorVoice;
    private BigDecimal rorSms;
    private BigDecimal monthlyFee;

    public RatePlan() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public BigDecimal getRorData() {
        return rorData;
    }

    public void setRorData(BigDecimal rorData) {
        this.rorData = rorData;
    }

    public BigDecimal getRorVoice() {
        return rorVoice;
    }

    public void setRorVoice(BigDecimal rorVoice) {
        this.rorVoice = rorVoice;
    }

    public BigDecimal getRorSms() {
        return rorSms;
    }

    public void setRorSms(BigDecimal rorSms) {
        this.rorSms = rorSms;
    }

    public BigDecimal getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(BigDecimal monthlyFee) {
        this.monthlyFee = monthlyFee;
    }
}
