package br.com.davibrito.rinha_backend_2025.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.davibrito.rinha_backend_2025.dtos.PaymentRequest;
import br.com.davibrito.rinha_backend_2025.dtos.PaymentSummaryResponse;
import br.com.davibrito.rinha_backend_2025.model.ProcessedPayment;
import br.com.davibrito.rinha_backend_2025.service.PaymentService;
import reactor.core.publisher.Mono;

/**
 * Controlador responsável pelos endpoints de pagamentos da API.
 * Implementa os endpoints conforme especificação da Rinha de Backend 2025.
 */
@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Endpoint para processar um novo pagamento.
     * 
     * @param paymentRequest os dados do pagamento a ser processado
     * @return o pagamento processado com status 201 (CREATED)
     */
    @PostMapping("/payments")
    public Mono<ResponseEntity<ProcessedPayment>> processPayment(@Validated @RequestBody PaymentRequest paymentRequest) {
        return paymentService.processPayment(paymentRequest)
                .map(payment -> ResponseEntity.status(HttpStatus.CREATED).body(payment))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
    }

    /**
     * Endpoint para obter o resumo de pagamentos.
     * 
     * @return o resumo dos pagamentos com status 200 (OK)
     */
    @GetMapping("/payments-summary")
    public Mono<ResponseEntity<PaymentSummaryResponse>> getPaymentsSummary() {
        return paymentService.getPaymentsSummary()
                .map(summary -> ResponseEntity.ok(summary))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint secreto para limpeza de dados usado pelos testes.
     * Obrigatório conforme especificação da Rinha de Backend 2025.
     * 
     * @return status 200 (OK) após limpeza
     */
    @PostMapping("/purge-payments")
    public Mono<ResponseEntity<Void>> purgePayments() {
        return paymentService.purgeAllPayments()
                .then(Mono.just(ResponseEntity.ok().build()));
    }
}