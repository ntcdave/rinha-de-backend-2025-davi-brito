package br.com.davibrito.rinha_backend_2025.service;

import br.com.davibrito.rinha_backend_2025.dtos.PaymentRequest;
import br.com.davibrito.rinha_backend_2025.model.PaymentEntity;
import br.com.davibrito.rinha_backend_2025.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Serviço responsável pelo processamento assíncrono de pagamentos.
 * Implementa a lógica de escolha do processador conforme regra 2.
 */
@Service
public class PaymentProcessingService {

    private final WebClient defaultProcessorClient;
    private final WebClient fallbackProcessorClient;
    private final CircuitBreakerService circuitBreakerService;
    private final PaymentRepository paymentRepository;

    // Fila interna para processamento assíncrono (regra 1)
    private final BlockingQueue<PaymentRequest> paymentQueue = new LinkedBlockingQueue<>();

    @Autowired
    public PaymentProcessingService(
            @Value("${rinha.processor.default.url}") String defaultUrl,
            @Value("${rinha.processor.fallback.url}") String fallbackUrl,
            CircuitBreakerService circuitBreakerService,
            PaymentRepository paymentRepository,
            WebClient.Builder webClientBuilder) {

        this.defaultProcessorClient = webClientBuilder
                .baseUrl(defaultUrl)
                .build();
        this.fallbackProcessorClient = webClientBuilder
                .baseUrl(fallbackUrl)
                .build();
        this.circuitBreakerService = circuitBreakerService;
        this.paymentRepository = paymentRepository;

        // Inicia o processamento assíncrono da fila
        startPaymentProcessing();
    }

    /**
     * Adiciona um pagamento à fila para processamento assíncrono.
     * Conforme regra 1: não espera o processamento completo.
     */
    public Mono<Void> enqueuePayment(PaymentRequest paymentRequest) {
        return Mono.fromCallable(() -> {
            paymentQueue.offer(paymentRequest);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Inicia o processamento assíncrono da fila de pagamentos.
     * Roda em background consumindo a fila continuamente.
     */
    private void startPaymentProcessing() {
        Mono.fromCallable(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    PaymentRequest paymentRequest = paymentQueue.take(); // Bloqueia até ter item
                    processPaymentInternal(paymentRequest).subscribe();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            return null;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();
    }

    /**
     * Processa um pagamento seguindo a estratégia de escolha do processador.
     * Implementa a regra 2: prioriza default, fallback em caso de falha.
     */
    private Mono<Void> processPaymentInternal(PaymentRequest paymentRequest) {
        // Verifica se o pagamento já foi processado (evita duplicação)
        return paymentRepository.existsByCorrelationId(paymentRequest.getCorrelationId())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.empty(); // Já processado, ignora
                    }

                    // Estratégia de escolha do processador
                    boolean useDefault = circuitBreakerService.shouldUseDefault();

                    if (useDefault) {
                        return tryDefaultProcessor(paymentRequest);
                    } else {
                        return useFallbackProcessor(paymentRequest);
                    }
                });
    }

    /**
     * Tenta processar o pagamento no processador default.
     */
    private Mono<Void> tryDefaultProcessor(PaymentRequest paymentRequest) {
        return sendPaymentToProcessor(defaultProcessorClient, paymentRequest)
                .flatMap(success -> {
                    if (success) {
                        // Sucesso no default
                        circuitBreakerService.recordSuccess();
                        return saveProcessedPayment(paymentRequest, "default");
                    } else {
                        // Falha no default - registra falha e tenta fallback
                        circuitBreakerService.recordFailure();
                        return useFallbackProcessor(paymentRequest);
                    }
                })
                .onErrorResume(error -> {
                    // Erro no default - registra falha e tenta fallback
                    circuitBreakerService.recordFailure();
                    return useFallbackProcessor(paymentRequest);
                });
    }

    /**
     * Usa o processador fallback.
     */
    private Mono<Void> useFallbackProcessor(PaymentRequest paymentRequest) {
        return sendPaymentToProcessor(fallbackProcessorClient, paymentRequest)
                .flatMap(success -> {
                    if (success) {
                        return saveProcessedPayment(paymentRequest, "fallback");
                    } else {
                        // Falha em ambos os processadores - log do erro
                        return Mono.empty();
                    }
                })
                .onErrorResume(error -> Mono.empty()); // Não propaga erro para não travar o processamento
    }

    /**
     * Envia o pagamento para um processador específico.
     */
    private Mono<Boolean> sendPaymentToProcessor(WebClient client, PaymentRequest paymentRequest) {
        return client
                .post()
                .bodyValue(paymentRequest)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(5)) // Timeout para detectar lentidão
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false); // Qualquer erro = falha
    }

    /**
     * Salva o pagamento processado no banco de dados.
     * Conforme regra 4: só salva após confirmação de sucesso.
     */
    private Mono<Void> saveProcessedPayment(PaymentRequest paymentRequest, String processorUsed) {
        PaymentEntity entity = new PaymentEntity(
                paymentRequest.getCorrelationId(),
                paymentRequest.getAmount(),
                LocalDateTime.now(),
                processorUsed
        );

        return paymentRepository.save(entity)
                .then();
    }
}