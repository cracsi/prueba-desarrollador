package com.seguros.polizas;

import com.seguros.polizas.core.CorePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PolizasApiIntegrationTest {

    private static final String API_KEY = "x-api-key";
    private static final String KEY = "123456";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CorePort corePort;

    @Test
    void sinApiKeyResponde401() throws Exception {
        mvc.perform(get("/polizas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listaPolizasFiltrandoPorTipoYEstado() throws Exception {
        mvc.perform(get("/polizas").header(API_KEY, KEY)
                        .param("tipo", "INDIVIDUAL").param("estado", "ACTIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numero", is("IND-0001")));
    }

    @Test
    void renovarIncrementaCanonYNotificaAlCore() throws Exception {
        mvc.perform(post("/polizas/1/renovar").header(API_KEY, KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("RENOVADA")))
                .andExpect(jsonPath("$.canonMensual", is(1578000.00)))
                .andExpect(jsonPath("$.prima", is(18936000.00)));

        verify(corePort).notificarActualizacion(1L);
    }

    @Test
    void renovarPolizaCanceladaResponde422YNoNotificaAlCore() throws Exception {
        mvc.perform(post("/polizas/3/renovar").header(API_KEY, KEY))
                .andExpect(status().isUnprocessableEntity());

        verify(corePort, never()).notificarActualizacion(anyLong());
    }

    @Test
    void cancelarPolizaCancelaSusRiesgos() throws Exception {
        mvc.perform(post("/polizas/2/cancelar").header(API_KEY, KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADA")));

        mvc.perform(get("/polizas/2/riesgos").header(API_KEY, KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].estado", everyItem(is("CANCELADO"))));
    }

    @Test
    void agregarRiesgoAColectivaResponde201() throws Exception {
        mvc.perform(post("/polizas/2/riesgos").header(API_KEY, KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"direccionInmueble":"Calle 10 # 5-20","arrendatario":"Laura","arrendador":"Andrés"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.polizaId", is(2)))
                .andExpect(jsonPath("$.estado", is("ACTIVO")));

        verify(corePort).notificarActualizacion(2L);
    }

    @Test
    void agregarRiesgoAIndividualResponde422() throws Exception {
        mvc.perform(post("/polizas/1/riesgos").header(API_KEY, KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"direccionInmueble":"Calle 10 # 5-20","arrendatario":"Laura","arrendador":"Andrés"}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void agregarRiesgoSinDatosResponde400() throws Exception {
        mvc.perform(post("/polizas/2/riesgos").header(API_KEY, KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelarRiesgoDeColectiva() throws Exception {
        mvc.perform(post("/riesgos/2/cancelar").header(API_KEY, KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADO")));

        verify(corePort).notificarActualizacion(2L);
    }

    @Test
    void polizaInexistenteResponde404() throws Exception {
        mvc.perform(get("/polizas/999/riesgos").header(API_KEY, KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void coreMockRegistraElEvento() throws Exception {
        mvc.perform(post("/core-mock/evento").header(API_KEY, KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"evento":"ACTUALIZACION","polizaId":555}
                                """))
                .andExpect(status().isAccepted());
    }
}
