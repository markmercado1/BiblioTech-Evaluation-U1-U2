package upeu.bibliotech_v2.integracion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import upeu.bibliotech_v2.controller.PrestamoController;
import upeu.bibliotech_v2.exception.PrestamoNoEncontradoException;
import upeu.bibliotech_v2.exception.PrestamoNoPermitidoException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.model.Prestamo;
import upeu.bibliotech_v2.service.PrestamoService;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** RN-03 y RN-04 vistas desde el controlador: registro y devolución de préstamos. */
@WebMvcTest(PrestamoController.class)
class PrestamoControllerReglasTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrestamoService prestamoService;

    private Prestamo prestamo(String estado) {
        return new Prestamo(new Estudiante("70000001", "Ana", "ACTIVO"),
                new Libro("ISBN-1", "Clean Code", "Robert Martin", 2), LocalDate.now(), estado);
    }

    @Test
    @DisplayName("POST /api/prestamos autorizado responde 200 con el préstamo ACTIVO")
    void registroDePrestamoResponde200() throws Exception {
        // Arrange
        when(prestamoService.registrarPrestamo(1L, 10L)).thenReturn(prestamo("ACTIVO"));

        // Act + Assert
        mockMvc.perform(post("/api/prestamos")
                        .param("estudianteId", "1")
                        .param("libroId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("POST /api/prestamos rechazado por una regla responde 400 con el mensaje")
    void registroRechazadoResponde400() throws Exception {
        // Arrange
        when(prestamoService.registrarPrestamo(1L, 10L))
                .thenThrow(new PrestamoNoPermitidoException("El libro no tiene ejemplares disponibles"));

        // Act + Assert
        mockMvc.perform(post("/api/prestamos")
                        .param("estudianteId", "1")
                        .param("libroId", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El libro no tiene ejemplares disponibles"));
    }

    @Test
    @DisplayName("PUT /api/prestamos/{id}/devolucion responde 200 con el préstamo DEVUELTO")
    void devolucionResponde200() throws Exception {
        // Arrange
        when(prestamoService.devolverLibro(5L)).thenReturn(prestamo("DEVUELTO"));

        // Act + Assert
        mockMvc.perform(put("/api/prestamos/5/devolucion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTO"));
    }

    @Test
    @DisplayName("PUT /api/prestamos/{id}/devolucion de un préstamo inexistente responde 404")
    void devolucionDePrestamoInexistenteResponde404() throws Exception {
        // Arrange
        when(prestamoService.devolverLibro(99L)).thenThrow(new PrestamoNoEncontradoException());

        // Act + Assert
        mockMvc.perform(put("/api/prestamos/99/devolucion"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Préstamo no existe"));
    }

    @Test
    @DisplayName("PUT /api/prestamos/{id}/devolucion de un préstamo ya devuelto responde 400")
    void devolucionDePrestamoYaDevueltoResponde400() throws Exception {
        // Arrange
        when(prestamoService.devolverLibro(5L))
                .thenThrow(new PrestamoNoPermitidoException("El préstamo ya fue devuelto"));

        // Act + Assert
        mockMvc.perform(put("/api/prestamos/5/devolucion"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El préstamo ya fue devuelto"));
    }
}
