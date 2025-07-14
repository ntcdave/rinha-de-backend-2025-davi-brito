package br.com.davibrito.rinha_backend_2025.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.davibrito.rinha_backend_2025.dtos.PaymentRequest;
import br.com.davibrito.rinha_backend_2025.dtos.PaymentSummaryResponse;
import br.com.davibrito.rinha_backend_2025.model.ProcessedPayment;
import br.com.davibrito.rinha_backend_2025.repository.PaymentRepository;
import reactor.core.publisher.Mono;

/**
 * Implementação do serviço de pagamentos.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentProcessingService paymentProcessingService;
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentServiceImpl(PaymentProcessingService paymentProcessingService, 
                             PaymentRepository paymentRepository) {
        this.paymentProcessingService = paymentProcessingService;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Mono<ProcessedPayment> processPayment(PaymentRequest paymentRequest) {
        // Adiciona à fila assíncrona e retorna resposta imediata
        return paymentProcessingService.enqueuePayment(paymentRequest)
                .then(Mono.fromCallable(() -> {
                    // Retorna um ProcessedPayment com os dados da requisição
                    ProcessedPayment processed = new ProcessedPayment();
                    processed.setId(paymentRequest.getCorrelationId().toString());
                    processed.setAmount(paymentRequest.getAmount());
                    processed.setStatus("PROCESSING");
                    processed.setTimestamp(LocalDateTime.now());
                    processed.setDescription("Payment enqueued for processing");
                    return processed;
                }));
    }

    @Override
    public Mono<PaymentSummaryResponse> getPaymentsSummary() {
        // Busca totais do processador default
        Mono<PaymentSummaryResponse.ProcessorSummary> defaultSummary = 
                Mono.zip(
                    paymentRepository.countDefaultProcessorRequests(),
                    paymentRepository.sumDefaultProcessorAmount()
                )
                .map(tuple -> new PaymentSummaryResponse.ProcessorSummary(
                    tuple.getT1(), 
                    BigDecimal.valueOf(tuple.getT2())
                ));

        // Busca totais do processador fallback
        Mono<PaymentSummaryResponse.ProcessorSummary> fallbackSummary = 
                Mono.zip(
                    paymentRepository.countFallbackProcessorRequests(),
                    paymentRepository.sumFallbackProcessorAmount()
                )
                .map(tuple -> new PaymentSummaryResponse.ProcessorSummary(
                    tuple.getT1(), 
                    BigDecimal.valueOf(tuple.getT2())
                ));

        // Combina ambos os resultados
        return Mono.zip(defaultSummary, fallbackSummary)
                .map(tuple -> new PaymentSummaryResponse(tuple.getT1(), tuple.getT2()));
    }

    @Override
    public Mono<Void> purgeAllPayments() {
        // Remove todos os pagamentos do banco de dados
        return paymentRepository.deleteAll();
    }
}
