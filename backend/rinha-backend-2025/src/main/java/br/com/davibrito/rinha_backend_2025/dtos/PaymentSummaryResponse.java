package br.com.davibrito.rinha_backend_2025.dtos;

import java.math.BigDecimal;

/**
 * DTO para fornecer resumo de pagamentos.
 */
public class PaymentSummaryResponse {
    private BigDecimal totalAmount;
    private long totalCount;
    private BigDecimal averageAmount;
    private BigDecimal maxAmount;
    private BigDecimal minAmount;

    public PaymentSummaryResponse() {
    }

    public PaymentSummaryResponse(BigDecimal totalAmount, long totalCount, BigDecimal averageAmount, 
                                 BigDecimal maxAmount, BigDecimal minAmount) {
        this.totalAmount = totalAmount;
        this.totalCount = totalCount;
        this.averageAmount = averageAmount;
        this.maxAmount = maxAmount;
        this.minAmount = minAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public BigDecimal getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(BigDecimal averageAmount) {
        this.averageAmount = averageAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }
}