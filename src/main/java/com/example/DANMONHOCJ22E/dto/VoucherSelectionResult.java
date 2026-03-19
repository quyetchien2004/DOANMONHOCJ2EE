package com.example.DANMONHOCJ22E.dto;

import java.math.BigDecimal;

public class VoucherSelectionResult {

    private String appliedCode;
    private Long userVoucherId;
    private BigDecimal discountAmount;

    public VoucherSelectionResult() {
        this.discountAmount = BigDecimal.ZERO;
    }

    public String getAppliedCode() {
        return appliedCode;
    }

    public void setAppliedCode(String appliedCode) {
        this.appliedCode = appliedCode;
    }

    public Long getUserVoucherId() {
        return userVoucherId;
    }

    public void setUserVoucherId(Long userVoucherId) {
        this.userVoucherId = userVoucherId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
}
