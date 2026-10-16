package com.seguros.polizas.domain;

import jakarta.persistence.*;

/**
 * Un riesgo es un inmueble arrendado asegurado dentro de una póliza.
 * Asegurado: arrendatario. Beneficiario: arrendador.
 */
@Entity
@Table(name = "riesgo")
public class Riesgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poliza_id")
    private Poliza poliza;

    @Column(name = "direccion_inmueble", nullable = false)
    private String direccionInmueble;

    @Column(nullable = false)
    private String arrendatario;

    @Column(nullable = false)
    private String arrendador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRiesgo estado;

    @Version
    private Long version;

    protected Riesgo() {
        // requerido por JPA
    }

    public Riesgo(String direccionInmueble, String arrendatario, String arrendador) {
        this.direccionInmueble = direccionInmueble;
        this.arrendatario = arrendatario;
        this.arrendador = arrendador;
        this.estado = EstadoRiesgo.ACTIVO;
    }

    public void cancelar() {
        if (estado == EstadoRiesgo.CANCELADO) {
            throw new ReglaNegocioException("El riesgo " + id + " ya está cancelado");
        }
        this.estado = EstadoRiesgo.CANCELADO;
    }

    public boolean estaActivo() {
        return estado == EstadoRiesgo.ACTIVO;
    }

    void asignarPoliza(Poliza poliza) {
        this.poliza = poliza;
    }

    public Long getId() { return id; }
    public Poliza getPoliza() { return poliza; }
    public String getDireccionInmueble() { return direccionInmueble; }
    public String getArrendatario() { return arrendatario; }
    public String getArrendador() { return arrendador; }
    public EstadoRiesgo getEstado() { return estado; }
}
