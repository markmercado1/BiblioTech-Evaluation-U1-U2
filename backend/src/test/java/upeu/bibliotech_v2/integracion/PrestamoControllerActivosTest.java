package upeu.bibliotech_v2.integracion;

import upeu.bibliotech_v2.controller.PrestamoController;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import upeu.bibliotech_v2.exception.EstudianteNoEncontradoException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.model.Prestamo;
import upeu.bibliotech_v2.service.PrestamoService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrestamoController.class)
class PrestamoControllerActivosTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrestamoService prestamoService;

    @Test
    @DisplayName("GET /api/prestamos/activos responde 200 con la lista de préstamos activos")
    void debeResponderConLosPrestamosActivos() throws Exception {
        // Arrange
        Prestamo prestamo = new Prestamo(
                new Estudiante("Juan Perez", "ACTIVO"),
                new Libro("Programacion Java", "Deitel", 3),
                LocalDate.of(2026, 9, 17), "ACTIVO");
        when(prestamoService.listarPrestamosActivos(1L)).thenReturn(List.of(prestamo));

        // Act + Assert
        mockMvc.perform(get("/api/prestamos/activos").param("estudianteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"))
                .andExpect(jsonPath("$[0].libro.titulo").value("Programacion Java"));
    }

    @Test
    @DisplayName("GET /api/prestamos/activos responde 404 si el estudiante no existe")
    void debeResponder404SiElEstudianteNoExiste() throws Exception {
        // Arrange
        when(prestamoService.listarPrestamosActivos(999L))
                .thenThrow(new EstudianteNoEncontradoException());

        // Act + Assert
        mockMvc.perform(get("/api/prestamos/activos").param("estudianteId", "999"))
                .andExpect(status().isNotFound());
    }
}
