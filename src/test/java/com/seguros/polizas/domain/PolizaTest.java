package com.seguros.polizas.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas unitarias de las reglas de negocio, sin Spring ni base de datos. */
class PolizaTest {

    private Poliza colectiva() {
        return new Poliza("COL-1", TipoPoliza.COLECTIVA, "Inmobiliaria", LocalDate.of(2026, 1, 1), 12,
                new BigDecimal("1000000"));
    }

    private Poliza individual() {
        return new Poliza("IND-1", TipoPoliza.INDIVIDUAL, "Arrendatario", LocalDate.of(2026, 1, 1), 12,
                new BigDecimal("1000000"));
    }

    private Riesgo riesgo() {
        return new Riesgo("Calle 1 # 2-3", "Arrendatario", "Arrendador");
    }

    @Test
    void primaEsCanonPorMesesDeVigencia() {
        assertThat(colectiva().getPrima()).isEqualByComparingTo("12000000");
    }

    @Test
    void renovarAplicaIpcAlCanonYPrimaYCambiaEstadoYVigencia() {
        Poliza poliza = colectiva();

        poliza.renovar(new BigDecimal("0.052"));

        assertThat(poliza.getCanonMensual()).isEqualByComparingTo("1052000");
        assertThat(poliza.getPrima()).isEqualByComparingTo("12624000");
        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.RENOVADA);
        assertThat(poliza.getFechaInicio()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(poliza.getFechaFin()).isEqualTo(LocalDate.of(2027, 12, 31));
    }

    @Test
    void noSePuedeRenovarUnaPolizaCancelada() {
        Poliza poliza = colectiva();
        poliza.cancelar();

        assertThatThrownBy(() -> poliza.renovar(new BigDecimal("0.052")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    void cancelarPolizaCancelaTodosSusRiesgos() {
        Poliza poliza = colectiva();
        poliza.agregarRiesgo(riesgo());
        poliza.agregarRiesgo(riesgo());

        poliza.cancelar();

        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.CANCELADA);
        assertThat(poliza.getRiesgos()).allMatch(r -> r.getEstado() == EstadoRiesgo.CANCELADO);
    }

    @Test
    void colectivaPermiteVariosRiesgos() {
        Poliza poliza = colectiva();
        poliza.agregarRiesgo(riesgo());
        poliza.agregarRiesgo(riesgo());

        assertThat(poliza.getRiesgos()).hasSize(2);
    }

    @Test
    void noSePuedenAgregarRiesgosAUnaIndividual() {
        assertThatThrownBy(() -> individual().agregarRiesgo(riesgo()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("colectivas");
    }

    @Test
    void noSePuedenAgregarRiesgosAUnaPolizaCancelada() {
        Poliza poliza = colectiva();
        poliza.cancelar();

        assertThatThrownBy(() -> poliza.agregarRiesgo(riesgo()))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
