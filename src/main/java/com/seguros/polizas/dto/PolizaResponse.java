package com.seguros.polizas.dto;

import com.seguros.polizas.domain.EstadoPoliza;
import com.seguros.polizas.domain.Poliza;
import com.seguros.polizas.domain.TipoPoliza;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolizaResponse(
        Long id,
        String numero,
        TipoPoliza tipo,
        String tomador,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        int mesesVigencia,
        BigDecimal canonMensual,
        BigDecimal prima,
        EstadoPoliza estado
) {
    public static PolizaResponse desde(Poliza p) {
        return new PolizaResponse(p.getId(), p.getNumero(), p.getTipo(), p.getTomador(),
                p.getFechaInicio(), p.getFechaFin(), p.getMesesVigencia(),
                p.getCanonMensual(), p.getPrima(), p.getEstado());
    }
}
