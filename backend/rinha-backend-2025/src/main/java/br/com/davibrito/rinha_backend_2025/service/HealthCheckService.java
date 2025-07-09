package br.com.davibrito.rinha_backend_2025.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Serviço para monitorar o health-check dos processadores.
 * Conforme regra 3: consulta a cada 5 segundos respeitando o rate limit.
 */
@Service
public class HealthCheckService {

    private final WebClient defaultHealthCheckClient;
    private final CircuitBreakerService circuitBreakerService;

    private final AtomicBoolean defaultProcessorFailing = new AtomicBoolean(false);
    private final AtomicInteger defaultMinResponseTime = new AtomicInteger(0);

    @Autowired
    public HealthCheckService(
            @Value("${rinha.healthcheck.default.url}") String defaultHealthUrl,
            CircuitBreakerService circuitBreakerService,
            WebClient.Builder webClientBuilder) {

        this.defaultHealthCheckClient = webClientBuilder
                .baseUrl(defaultHealthUrl)
                .build();
        this.circuitBreakerService = circuitBreakerService;
    }

    /**
     * Executa health-check a cada 5 segundos conforme especificação da Rinha.
     * Usa @Scheduled para garantir execução regular.
     */
    @Scheduled(fixedDelay = 5000) // 5 segundos
    public void checkDefaultProcessorHealth() {
        defaultHealthCheckClient
                .get()
                .retrieve()
                .bodyToMono(HealthCheckResponse.class)
                .timeout(Duration.ofSeconds(3)) // Timeout para evitar travamento
                .doOnNext(this::updateDefaultProcessorStatus)
                .doOnError(this::handleHealthCheckError)
                .onErrorResume(e -> Mono.empty()) // Não propaga erro para não travar o scheduler
                .subscribe();
    }

    /**
     * Atualiza o status do processador default baseado no health-check.
     */
    private void updateDefaultProcessorStatus(HealthCheckResponse response) {
        boolean failing = response.failing;
        int minResponseTime = response.minResponseTime;

        defaultProcessorFailing.set(failing);
        defaultMinResponseTime.set(minResponseTime);

        // Atualiza o circuit breaker com a informação do health-check
        circuitBreakerService.updateHealthCheckStatus(failing);
    }

    /**
     * Trata erros no health-check (ex: HTTP 429 - Too Many Requests).
     */
    private void handleHealthCheckError(Throwable error) {
        // Em caso de erro no health-check, assume que o processador está falhando
        defaultProcessorFailing.set(true);
        circuitBreakerService.updateHealthCheckStatus(true);
    }

    /**
     * Verifica se o processador default está falhando.
     */
    public boolean isDefaultProcessorFailing() {
        return defaultProcessorFailing.get();
    }

    /**
     * Obtém o tempo mínimo de resposta do processador default.
     */
    public int getDefaultMinResponseTime() {
        return defaultMinResponseTime.get();
    }

    /**
     * Obtém as métricas atuais do health-check.
     */
    public Mono<HealthCheckMetrics> getMetrics() {
        return Mono.just(new HealthCheckMetrics(
            defaultProcessorFailing.get(),
            defaultMinResponseTime.get()
        ));
    }

    /**
     * DTO para resposta do health-check dos processadores.
     */
    public static class HealthCheckResponse {
        public boolean failing;
        public int minResponseTime;

        public HealthCheckResponse() {
        }

        public HealthCheckResponse(boolean failing, int minResponseTime) {
            this.failing = failing;
            this.minResponseTime = minResponseTime;
        }
    }

    /**
     * Métricas do health-check.
     */
    public static class HealthCheckMetrics {
        private final boolean failing;
        private final int minResponseTime;

        public HealthCheckMetrics(boolean failing, int minResponseTime) {
            this.failing = failing;
            this.minResponseTime = minResponseTime;
        }

        public boolean isFailing() { return failing; }
        public int getMinResponseTime() { return minResponseTime; }
    }
}