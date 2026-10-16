package com.seguros.polizas.domain;

/**
 * Se lanza cuando una operación viola una regla de negocio del dominio.
 * Se traduce a HTTP 422 (Unprocessable Entity).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
