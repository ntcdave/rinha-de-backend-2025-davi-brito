package br.com.davibrito.rinha_backend_2025.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Classe que representa um pagamento processado.
 */
public class ProcessedPayment {
    private String id;
    private BigDecimal amount;
    private String description;
    private LocalDateTime timestamp;
    private String status;

    public ProcessedPayment() {
    }

    public ProcessedPayment(String id, BigDecimal amount, String description, LocalDateTime timestamp, String status) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}