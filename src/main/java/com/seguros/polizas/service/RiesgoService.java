package com.seguros.polizas.service;

import com.seguros.polizas.core.CorePort;
import com.seguros.polizas.domain.Poliza;
import com.seguros.polizas.domain.ReglaNegocioException;
import com.seguros.polizas.domain.Riesgo;
import com.seguros.polizas.dto.CrearRiesgoRequest;
import com.seguros.polizas.dto.RiesgoResponse;
import com.seguros.polizas.repository.RiesgoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiesgoService {

    private static final Logger log = LoggerFactory.getLogger(RiesgoService.class);

    private final PolizaService polizaService;
    private final RiesgoRepository riesgoRepository;
    private final CorePort corePort;

    public RiesgoService(PolizaService polizaService, RiesgoRepository riesgoRepository, CorePort corePort) {
        this.polizaService = polizaService;
        this.riesgoRepository = riesgoRepository;
        this.corePort = corePort;
    }

    @Transactional
    public RiesgoResponse agregar(Long polizaId, CrearRiesgoRequest request) {
        Poliza poliza = polizaService.buscarPoliza(polizaId);
        Riesgo riesgo = new Riesgo(request.direccionInmueble(), request.arrendatario(), request.arrendador());
        poliza.agregarRiesgo(riesgo);          // valida tipo de póliza y estado
        riesgoRepository.saveAndFlush(riesgo); // flush para obtener el id generado
        log.info("Riesgo {} agregado a la póliza {}", riesgo.getId(), poliza.getNumero());
        corePort.notificarActualizacion(polizaId);
        return RiesgoResponse.desde(riesgo);
    }

    /**
     * Solo se cancelan riesgos individualmente en pólizas colectivas.
     * En una individual el riesgo se cancela cancelando la póliza.
     */
    @Transactional
    public RiesgoResponse cancelar(Long riesgoId) {
        Riesgo riesgo = riesgoRepository.findById(riesgoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el riesgo con id " + riesgoId));
        Poliza poliza = riesgo.getPoliza();
        if (!poliza.esColectiva()) {
            throw new ReglaNegocioException(
                    "El riesgo de una póliza individual se cancela cancelando la póliza " + poliza.getNumero());
        }
        riesgo.cancelar();
        log.info("Riesgo {} de la póliza {} cancelado", riesgoId, poliza.getNumero());
        corePort.notificarActualizacion(poliza.getId());
        return RiesgoResponse.desde(riesgo);
    }
}
