package br.com.davibrito.rinha_backend_2025.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para receber requisições de pagamento conforme especificação da Rinha.
 * Deve conter apenas correlationId (UUID) e amount (Decimal).
 */
public class PaymentRequest {

    @NotNull(message = "O correlationId é obrigatório")
    private UUID correlationId;

    @NotNull(message = "O valor do pagamento é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor do pagamento deve ser maior que zero")
    private BigDecimal amount;

    public PaymentRequest() {
    }

    public PaymentRequest(UUID correlationId, BigDecimal amount) {
        this.correlationId = correlationId;
        this.amount = amount;
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
}