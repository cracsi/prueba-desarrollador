package com.seguros.polizas.core;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Simula el servicio agnóstico de edición del CORE (WebLogic).
 * Su único propósito es registrar en logs el intento de envío.
 */
@RestController
@RequestMapping("/core-mock")
public class CoreMockController {

    private static final Logger log = LoggerFactory.getLogger(CoreMockController.class);

    @PostMapping("/evento")
    public ResponseEntity<Map<String, String>> recibirEvento(@Valid @RequestBody EventoCoreRequest request) {
        log.info("[CORE-MOCK] Evento recibido para enviar al CORE -> evento={}, polizaId={}",
                request.evento(), request.polizaId());
        return ResponseEntity.accepted().body(Map.of("resultado", "REGISTRADO"));
    }
}
