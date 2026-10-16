package com.seguros.polizas.core;

/**
 * Puerto (arquitectura hexagonal) hacia el servicio agnóstico de edición del CORE.
 * El negocio solo conoce esta interfaz; la implementación concreta (mock local
 * o servicio real en WebLogic) es un adaptador intercambiable.
 */
public interface CorePort {

    void notificarActualizacion(Long polizaId);
}
