package br.com.davibrito.rinha_backend_2025.dtos;

import java.math.BigDecimal;

/**
 * DTO para resumo de pagamentos conforme especificação da Rinha.
 * Deve mostrar totais por processador (default e fallback).
 */
public class PaymentSummaryResponse {

    private ProcessorSummary defaultProcessor;
    private ProcessorSummary fallbackProcessor;

    public PaymentSummaryResponse() {
    }

    public PaymentSummaryResponse(ProcessorSummary defaultProcessor, ProcessorSummary fallbackProcessor) {
        this.defaultProcessor = defaultProcessor;
        this.fallbackProcessor = fallbackProcessor;
    }

    public ProcessorSummary getDefaultProcessor() {
        return defaultProcessor;
    }

    public void setDefaultProcessor(ProcessorSummary defaultProcessor) {
        this.defaultProcessor = defaultProcessor;
    }

    public ProcessorSummary getFallbackProcessor() {
        return fallbackProcessor;
    }

    public void setFallbackProcessor(ProcessorSummary fallbackProcessor) {
        this.fallbackProcessor = fallbackProcessor;
    }

    /**
     * Classe interna para representar o resumo de cada processador.
     */
    public static class ProcessorSummary {
        private long totalRequests;
        private BigDecimal totalAmount;

        public ProcessorSummary() {
        }

        public ProcessorSummary(long totalRequests, BigDecimal totalAmount) {
            this.totalRequests = totalRequests;
            this.totalAmount = totalAmount;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public void setTotalRequests(long totalRequests) {
            this.totalRequests = totalRequests;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }
}