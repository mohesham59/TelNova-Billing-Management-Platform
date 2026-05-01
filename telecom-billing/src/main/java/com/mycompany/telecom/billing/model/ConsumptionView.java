package com.mycompany.telecom.billing.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author Ali
 */
/**
 * A read-only view of contract_consumption joined with service_package. Used
 * exclusively for the customer portal display.
 */
public class ConsumptionView {

    private String packageName;
    private String serviceType;   // voice | data | sms
    private BigDecimal totalQuota;
    private BigDecimal consumed;
    private LocalDate startingDate;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public BigDecimal getTotalQuota() {
        return totalQuota;
    }

    public void setTotalQuota(BigDecimal totalQuota) {
        this.totalQuota = totalQuota;
    }

    public BigDecimal getConsumed() {
        return consumed;
    }

    public void setConsumed(BigDecimal consumed) {
        this.consumed = consumed;
    }

    public LocalDate getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(LocalDate startingDate) {
        this.startingDate = startingDate;
    }

    /**
     * Returns 0–100 percentage consumed, capped at 100.
     */
    public int getPercentage() {
        if (totalQuota == null || totalQuota.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        if (consumed == null) {
            return 0;
        }
        int pct = consumed.multiply(BigDecimal.valueOf(100))
                .divide(totalQuota, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
        return Math.min(pct, 100);
    }

    /**
     * Remaining quota (never negative).
     *
     * @return
     */
    public BigDecimal getRemaining() {
        if (totalQuota == null) {
            return BigDecimal.ZERO;
        }
        if (consumed == null) {
            return totalQuota;
        }
        BigDecimal rem = totalQuota.subtract(consumed);
        return rem.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : rem;
    }

    public String getUnit() {
        if (serviceType == null) {
            return "";
        }
        return switch (serviceType) {
            case "voice" ->
                "min";
            case "data" ->
                "MB";
            case "sms" ->
                "msg";
            default ->
                "";
        };
    }
}
