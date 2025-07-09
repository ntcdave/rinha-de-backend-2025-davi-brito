package br.com.davibrito.rinha_backend_2025.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementação do padrão Circuit Breaker para o processador default.
 * Conforme regra 2: prioriza sempre o default, só usa fallback em caso de falha.
 */
@Service
public class CircuitBreakerService {

    private enum CircuitState {
        CLOSED,    // Normal: todas requisições vão para o default
        OPEN,      // Falha: todas requisições vão para o fallback
        HALF_OPEN  // Teste: envia uma requisição de teste para o default
    }

    // Configurações do Circuit Breaker
    private static final int FAILURE_THRESHOLD = 5; // Número de falhas para abrir o circuito
    private static final Duration TIMEOUT_DURATION = Duration.ofSeconds(10); // Tempo para tentar novamente
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(1); // Janela de tempo para contagem de falhas

    private final AtomicReference<CircuitState> state = new AtomicReference<>(CircuitState.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicReference<LocalDateTime> lastFailureTime = new AtomicReference<>(LocalDateTime.now());
    private final AtomicReference<LocalDateTime> lastOpenTime = new AtomicReference<>(null);
    private volatile boolean isHealthCheckFailing = false;

    /**
     * Verifica se deve usar o processador default ou fallback.
     */
    public boolean shouldUseDefault() {
        CircuitState currentState = state.get();

        switch (currentState) {
            case CLOSED:
                return !isHealthCheckFailing; // Só usa default se health-check estiver OK

            case OPEN:
                // Verifica se já passou o tempo de timeout para tentar novamente
                if (hasTimeoutExpired()) {
                    state.set(CircuitState.HALF_OPEN);
                    return true; // Tenta uma requisição de teste
                }
                return false; // Continua usando fallback

            case HALF_OPEN:
                return true; // Permite uma requisição de teste

            default:
                return false;
        }
    }

    /**
     * Registra uma falha no processador default.
     */
    public void recordFailure() {
        LocalDateTime now = LocalDateTime.now();
        lastFailureTime.set(now);

        // Só conta falhas dentro da janela de tempo
        if (isWithinFailureWindow(now)) {
            int currentFailures = failureCount.incrementAndGet();

            if (currentFailures >= FAILURE_THRESHOLD) {
                openCircuit();
            }
        } else {
            // Reset contador se a falha está fora da janela
            failureCount.set(1);
        }
    }

    /**
     * Registra um sucesso no processador default.
     */
    public void recordSuccess() {
        CircuitState currentState = state.get();

        if (currentState == CircuitState.HALF_OPEN) {
            // Sucesso na requisição de teste - fecha o circuito
            closeCircuit();
        }

        // Reset contador de falhas em caso de sucesso
        failureCount.set(0);
    }

    /**
     * Atualiza o estado baseado no health-check.
     */
    public void updateHealthCheckStatus(boolean failing) {
        this.isHealthCheckFailing = failing;

        if (failing && state.get() == CircuitState.CLOSED) {
            // Se health-check indica falha, abre o circuito proativamente
            openCircuit();
        }
    }

    /**
     * Obtém o estado atual do circuito.
     */
    public String getCircuitState() {
        return state.get().name();
    }

    /**
     * Obtém as métricas atuais do circuit breaker.
     */
    public Mono<CircuitBreakerMetrics> getMetrics() {
        return Mono.just(new CircuitBreakerMetrics(
            state.get().name(),
            failureCount.get(),
            isHealthCheckFailing,
            lastFailureTime.get(),
            lastOpenTime.get()
        ));
    }

    private void openCircuit() {
        state.set(CircuitState.OPEN);
        lastOpenTime.set(LocalDateTime.now());
    }

    private void closeCircuit() {
        state.set(CircuitState.CLOSED);
        failureCount.set(0);
        lastOpenTime.set(null);
    }

    private boolean hasTimeoutExpired() {
        LocalDateTime openTime = lastOpenTime.get();
        return openTime != null &&
               LocalDateTime.now().isAfter(openTime.plus(TIMEOUT_DURATION));
    }

    private boolean isWithinFailureWindow(LocalDateTime now) {
        LocalDateTime lastFailure = lastFailureTime.get();
        return lastFailure != null &&
               now.isBefore(lastFailure.plus(FAILURE_WINDOW));
    }

    /**
     * Classe para métricas do circuit breaker.
     */
    public static class CircuitBreakerMetrics {
        private final String state;
        private final int failureCount;
        private final boolean healthCheckFailing;
        private final LocalDateTime lastFailureTime;
        private final LocalDateTime lastOpenTime;

        public CircuitBreakerMetrics(String state, int failureCount, boolean healthCheckFailing,
                                   LocalDateTime lastFailureTime, LocalDateTime lastOpenTime) {
            this.state = state;
            this.failureCount = failureCount;
            this.healthCheckFailing = healthCheckFailing;
            this.lastFailureTime = lastFailureTime;
            this.lastOpenTime = lastOpenTime;
        }

        // Getters
        public String getState() { return state; }
        public int getFailureCount() { return failureCount; }
        public boolean isHealthCheckFailing() { return healthCheckFailing; }
        public LocalDateTime getLastFailureTime() { return lastFailureTime; }
        public LocalDateTime getLastOpenTime() { return lastOpenTime; }
    }
}