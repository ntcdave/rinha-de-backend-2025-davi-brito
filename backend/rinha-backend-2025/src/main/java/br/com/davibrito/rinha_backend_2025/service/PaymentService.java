package br.com.davibrito.rinha_backend_2025.service;

import br.com.davibrito.rinha_backend_2025.dtos.PaymentRequest;
import br.com.davibrito.rinha_backend_2025.dtos.PaymentSummaryResponse;
import br.com.davibrito.rinha_backend_2025.model.ProcessedPayment;
import reactor.core.publisher.Mono;

/**
 * Interface para o serviço de pagamentos.
 */
public interface PaymentService {

    /**
     * Processa um pagamento a partir de uma requisição.
     * 
     * @param paymentRequest a requisição de pagamento
     * @return o pagamento processado
     */
    Mono<ProcessedPayment> processPayment(PaymentRequest paymentRequest);

    /**
     * Obtém o resumo de todos os pagamentos processados.
     * 
     * @return o resumo de pagamentos
     */
    Mono<PaymentSummaryResponse> getPaymentsSummary();
}
