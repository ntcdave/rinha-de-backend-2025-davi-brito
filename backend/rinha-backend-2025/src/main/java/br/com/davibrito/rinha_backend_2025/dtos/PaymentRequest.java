package br.com.davibrito.rinha_backend_2025.dtos;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO para receber requisições de pagamento.
 */
public class PaymentRequest {
    @NotNull(message = "O valor do pagamento é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor do pagamento deve ser maior que zero")
    private BigDecimal amount;

    @NotBlank(message = "A descrição do pagamento é obrigatória")
    @Size(min = 1, max = 255, message = "A descrição deve ter entre 1 e 255 caracteres")
    private String description;

    public PaymentRequest() {
    }

    public PaymentRequest(BigDecimal amount, String description) {
        this.amount = amount;
        this.description = description;
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
}