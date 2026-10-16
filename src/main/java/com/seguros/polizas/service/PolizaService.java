package com.seguros.polizas.service;

import com.seguros.polizas.core.CorePort;
import com.seguros.polizas.domain.EstadoPoliza;
import com.seguros.polizas.domain.Poliza;
import com.seguros.polizas.domain.TipoPoliza;
import com.seguros.polizas.dto.PolizaResponse;
import com.seguros.polizas.dto.RiesgoResponse;
import com.seguros.polizas.repository.PolizaRepository;
import com.seguros.polizas.repository.RiesgoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PolizaService {

    private static final Logger log = LoggerFactory.getLogger(PolizaService.class);

    private final PolizaRepository polizaRepository;
    private final RiesgoRepository riesgoRepository;
    private final CorePort corePort;
    private final BigDecimal ipc;

    public PolizaService(PolizaRepository polizaRepository,
                         RiesgoRepository riesgoRepository,
                         CorePort corePort,
                         @Value("${poliza.ipc}") BigDecimal ipc) {
        this.polizaRepository = polizaRepository;
        this.riesgoRepository = riesgoRepository;
        this.corePort = corePort;
        this.ipc = ipc;
    }

    @Transactional(readOnly = true)
    public List<PolizaResponse> listar(TipoPoliza tipo, EstadoPoliza estado) {
        return polizaRepository.buscar(tipo, estado).stream()
                .map(PolizaResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RiesgoResponse> listarRiesgos(Long polizaId) {
        buscarPoliza(polizaId);
        return riesgoRepository.findByPolizaIdOrderById(polizaId).stream()
                .map(RiesgoResponse::desde)
                .toList();
    }

    @Transactional
    public PolizaResponse renovar(Long polizaId) {
        Poliza poliza = buscarPoliza(polizaId);
        poliza.renovar(ipc);
        log.info("Póliza {} renovada con IPC {}. Nuevo canon={}, nueva prima={}",
                poliza.getNumero(), ipc, poliza.getCanonMensual(), poliza.getPrima());
        corePort.notificarActualizacion(polizaId);
        return PolizaResponse.desde(poliza);
    }

    @Transactional
    public PolizaResponse cancelar(Long polizaId) {
        Poliza poliza = buscarPoliza(polizaId);
        poliza.cancelar();
        log.info("Póliza {} cancelada junto con sus riesgos activos", poliza.getNumero());
        corePort.notificarActualizacion(polizaId);
        return PolizaResponse.desde(poliza);
    }

    Poliza buscarPoliza(Long polizaId) {
        return polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la póliza con id " + polizaId));
    }
}
