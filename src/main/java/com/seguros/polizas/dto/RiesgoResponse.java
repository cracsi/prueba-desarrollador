package com.seguros.polizas.dto;

import com.seguros.polizas.domain.EstadoRiesgo;
import com.seguros.polizas.domain.Riesgo;

public record RiesgoResponse(
        Long id,
        Long polizaId,
        String direccionInmueble,
        String arrendatario,
        String arrendador,
        EstadoRiesgo estado
) {
    public static RiesgoResponse desde(Riesgo r) {
        return new RiesgoResponse(r.getId(), r.getPoliza().getId(), r.getDireccionInmueble(),
                r.getArrendatario(), r.getArrendador(), r.getEstado());
    }
}
