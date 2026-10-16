package com.seguros.polizas.core;

import com.seguros.polizas.security.ApiKeyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptador que consume el servicio de edición del CORE vía HTTP.
 * En la prueba apunta al mock local (/core-mock/evento); en producción
 * apuntaría a la capa media en WebLogic cambiando solo la propiedad core.base-url.
 *
 * Un fallo del CORE no revierte la operación de negocio: se registra en log.
 * En producción esto se resolvería con outbox + reintentos + circuit breaker (ver módulo 1).
 */
@Component
public class CoreRestAdapter implements CorePort {

    private static final Logger log = LoggerFactory.getLogger(CoreRestAdapter.class);

    private final RestClient restClient = RestClient.create();
    private final Environment environment;
    private final String apiKey;

    public CoreRestAdapter(Environment environment, @Value("${seguridad.api-key}") String apiKey) {
        this.environment = environment;
        this.apiKey = apiKey;
    }

    @Override
    public void notificarActualizacion(Long polizaId) {
        EventoCoreRequest evento = new EventoCoreRequest("ACTUALIZACION", polizaId);
        try {
            restClient.post()
                    .uri(baseUrl() + "/core-mock/evento")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(ApiKeyFilter.HEADER, apiKey)
                    .body(evento)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("No fue posible notificar al CORE la actualización de la póliza {}: {}", polizaId, e.getMessage());
        }
    }

    private String baseUrl() {
        return environment.getProperty("core.base-url", "http://localhost:8080");
    }
}
