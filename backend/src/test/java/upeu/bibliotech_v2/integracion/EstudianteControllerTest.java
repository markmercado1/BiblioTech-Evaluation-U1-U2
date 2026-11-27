package upeu.bibliotech_v2.integracion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import upeu.bibliotech_v2.controller.EstudianteController;
import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.service.EstudianteService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstudianteController.class)
class EstudianteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstudianteService estudianteService;

    @Test
    @DisplayName("POST /api/estudiantes con datos válidos responde 201 y estado ACTIVO")
    void registroDeEstudianteResponde201() throws Exception {
        // Arrange
        when(estudianteService.registrar(any(Estudiante.class)))
                .thenReturn(new Estudiante("70000001", "Ana", "ACTIVO"));

        // Act + Assert
        mockMvc.perform(post("/api/estudiantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"70000001\",\"nombre\":\"Ana\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("POST /api/estudiantes con DNI repetido responde 400")
    void registroConDniRepetidoResponde400() throws Exception {
        // Arrange
        when(estudianteService.registrar(any(Estudiante.class)))
                .thenThrow(new DatoInvalidoException("El DNI ya está registrado"));

        // Act + Assert
        mockMvc.perform(post("/api/estudiantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"70000001\",\"nombre\":\"Ana\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El DNI ya está registrado"));
    }
}
