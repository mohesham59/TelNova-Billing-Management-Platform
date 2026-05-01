package com.mycompany.telecom.billing.model;

import java.math.BigDecimal;

/**
 *
 * @author Ali
 */
public class ServicePackage {

    private int id;
    private String name;
    private String type;      // 'voice' | 'data' | 'sms'
    private BigDecimal amount;
    private int priority;

    public ServicePackage() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
