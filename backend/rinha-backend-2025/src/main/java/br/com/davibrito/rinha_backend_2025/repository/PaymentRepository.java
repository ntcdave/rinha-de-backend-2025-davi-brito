package br.com.davibrito.rinha_backend_2025.repository;

import br.com.davibrito.rinha_backend_2025.model.PaymentEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repository reativo para operações com pagamentos processados.
 * Usa Spring Data R2DBC para integração assíncrona com PostgreSQL.
 */
public interface PaymentRepository extends R2dbcRepository<PaymentEntity, Long> {

    /**
     * Verifica se um pagamento já foi processado pelo correlationId.
     * Importante para evitar duplicações.
     */
    Mono<Boolean> existsByCorrelationId(UUID correlationId);

    /**
     * Calcula o total de requisições processadas pelo processador default.
     */
    @Query("SELECT COUNT(*) FROM processed_payments WHERE processor_used = 'default'")
    Mono<Long> countDefaultProcessorRequests();

    /**
     * Calcula o total de requisições processadas pelo processador fallback.
     */
    @Query("SELECT COUNT(*) FROM processed_payments WHERE processor_used = 'fallback'")
    Mono<Long> countFallbackProcessorRequests();

    /**
     * Calcula o valor total processado pelo processador default.
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM processed_payments WHERE processor_used = 'default'")
    Mono<Double> sumDefaultProcessorAmount();

    /**
     * Calcula o valor total processado pelo processador fallback.
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM processed_payments WHERE processor_used = 'fallback'")
    Mono<Double> sumFallbackProcessorAmount();

    /**
     * Calcula totais com filtro de data opcional.
     */
    @Query("SELECT COUNT(*) FROM processed_payments WHERE processor_used = :processor " +
           "AND (:from IS NULL OR processed_at >= :from) " +
           "AND (:to IS NULL OR processed_at <= :to)")
    Mono<Long> countByProcessorAndDateRange(@Param("processor") String processor,
                                           @Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM processed_payments WHERE processor_used = :processor " +
           "AND (:from IS NULL OR processed_at >= :from) " +
           "AND (:to IS NULL OR processed_at <= :to)")
    Mono<Double> sumByProcessorAndDateRange(@Param("processor") String processor,
                                           @Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);
}