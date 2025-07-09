package br.com.davibrito.rinha_backend_2025.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa um pagamento processado no banco de dados.
 * Conforme regra 4: deve ter correlationId, amount, processed_at e qual processador foi usado.
 */
@Table("processed_payments")
public class PaymentEntity {

    @Id
    private Long id;

    @Column("correlation_id")
    private UUID correlationId;

    @Column("amount")
    private BigDecimal amount;

    @Column("processed_at")
    private LocalDateTime processedAt;

    @Column("processor_used")
    private String processorUsed; // "default" ou "fallback"

    public PaymentEntity() {
    }

    public PaymentEntity(UUID correlationId, BigDecimal amount, LocalDateTime processedAt, String processorUsed) {
        this.correlationId = correlationId;
        this.amount = amount;
        this.processedAt = processedAt;
        this.processorUsed = processorUsed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getProcessorUsed() {
        return processorUsed;
    }

    public void setProcessorUsed(String processorUsed) {
        this.processorUsed = processorUsed;
    }
}