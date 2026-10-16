package com.seguros.polizas.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Agregado raíz. Las reglas de negocio viven aquí (modelo de dominio rico),
 * no en el controller ni dispersas en el servicio.
 */
@Entity
@Table(name = "poliza")
public class Poliza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPoliza tipo;

    /** Individual: el arrendatario. Colectiva: la inmobiliaria o administración. */
    @Column(nullable = false)
    private String tomador;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "meses_vigencia", nullable = false)
    private int mesesVigencia;

    @Column(name = "canon_mensual", nullable = false, precision = 15, scale = 2)
    private BigDecimal canonMensual;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prima;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPoliza estado;

    @Version
    private Long version;

    @OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Riesgo> riesgos = new ArrayList<>();

    protected Poliza() {
        // requerido por JPA
    }

    public Poliza(String numero, TipoPoliza tipo, String tomador, LocalDate fechaInicio,
                  int mesesVigencia, BigDecimal canonMensual) {
        this.numero = numero;
        this.tipo = tipo;
        this.tomador = tomador;
        this.fechaInicio = fechaInicio;
        this.mesesVigencia = mesesVigencia;
        this.fechaFin = fechaInicio.plusMonths(mesesVigencia).minusDays(1);
        this.canonMensual = canonMensual.setScale(2, RoundingMode.HALF_UP);
        this.prima = calcularPrima(this.canonMensual, mesesVigencia);
        this.estado = EstadoPoliza.ACTIVA;
    }

    // ---------- Reglas de negocio ----------

    /**
     * Renueva por el mismo periodo de vigencia inicial, ajustando el canon según el IPC.
     * Regla: no se puede renovar una póliza cancelada.
     */
    public void renovar(BigDecimal ipc) {
        if (estado == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("No se puede renovar la póliza " + numero + " porque está cancelada");
        }
        if (ipc == null || ipc.signum() < 0) {
            throw new ReglaNegocioException("El IPC debe ser un valor mayor o igual a cero");
        }
        this.canonMensual = canonMensual.multiply(BigDecimal.ONE.add(ipc)).setScale(2, RoundingMode.HALF_UP);
        this.prima = calcularPrima(canonMensual, mesesVigencia);
        this.fechaInicio = fechaFin.plusDays(1);
        this.fechaFin = fechaInicio.plusMonths(mesesVigencia).minusDays(1);
        this.estado = EstadoPoliza.RENOVADA;
    }

    /**
     * Regla: la cancelación de una póliza cancela todos sus riesgos.
     */
    public void cancelar() {
        if (estado == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("La póliza " + numero + " ya está cancelada");
        }
        this.estado = EstadoPoliza.CANCELADA;
        riesgos.stream()
                .filter(Riesgo::estaActivo)
                .forEach(Riesgo::cancelar);
    }

    /**
     * Reglas: solo las pólizas colectivas admiten agregar riesgos; una individual
     * solo puede tener un riesgo. No se agregan riesgos a pólizas canceladas.
     */
    public void agregarRiesgo(Riesgo riesgo) {
        if (estado == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("No se pueden agregar riesgos a la póliza cancelada " + numero);
        }
        if (tipo == TipoPoliza.INDIVIDUAL && !riesgos.isEmpty()) {
            throw new ReglaNegocioException("La póliza individual " + numero + " solo puede tener un riesgo");
        }
        if (tipo != TipoPoliza.COLECTIVA) {
            throw new ReglaNegocioException("Solo las pólizas colectivas permiten agregar riesgos");
        }
        riesgo.asignarPoliza(this);
        riesgos.add(riesgo);
    }

    public boolean esColectiva() {
        return tipo == TipoPoliza.COLECTIVA;
    }

    private static BigDecimal calcularPrima(BigDecimal canon, int meses) {
        return canon.multiply(BigDecimal.valueOf(meses)).setScale(2, RoundingMode.HALF_UP);
    }

    // ---------- Getters ----------

    public Long getId() { return id; }
    public String getNumero() { return numero; }
    public TipoPoliza getTipo() { return tipo; }
    public String getTomador() { return tomador; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public int getMesesVigencia() { return mesesVigencia; }
    public BigDecimal getCanonMensual() { return canonMensual; }
    public BigDecimal getPrima() { return prima; }
    public EstadoPoliza getEstado() { return estado; }
    public List<Riesgo> getRiesgos() { return List.copyOf(riesgos); }
}
