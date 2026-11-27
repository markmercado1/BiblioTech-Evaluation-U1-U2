package upeu.bibliotech_v2.integracion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import upeu.bibliotech_v2.controller.LibroController;
import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.exception.LibroNoEncontradoException;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.service.LibroService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibroController.class)
class LibroControllerTest {

    private static final String LIBRO_JSON =
            "{\"isbn\":\"ISBN-1\",\"titulo\":\"Clean Code\",\"autor\":\"Robert Martin\",\"ejemplaresDisponibles\":2}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibroService libroService;

    @Test
    @DisplayName("GET /api/libros responde 200 con la lista y el estado calculado")
    void listaDeLibrosResponde200() throws Exception {
        // Arrange
        when(libroService.listar()).thenReturn(List.of(
                new Libro("ISBN-1", "Clean Code", "Robert Martin", 2),
                new Libro("ISBN-2", "Refactoring", "Martin Fowler", 0)));

        // Act + Assert
        mockMvc.perform(get("/api/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].estado").value("DISPONIBLE"))
                .andExpect(jsonPath("$[1].estado").value("NO DISPONIBLE"));
    }

    @Test
    @DisplayName("POST /api/libros con datos válidos responde 201")
    void registroDeLibroResponde201() throws Exception {
        // Arrange
        when(libroService.registrar(any(Libro.class)))
                .thenReturn(new Libro("ISBN-1", "Clean Code", "Robert Martin", 2));

        // Act + Assert
        mockMvc.perform(post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LIBRO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").value("ISBN-1"));
    }

    @Test
    @DisplayName("POST /api/libros con dato inválido responde 400 con el mensaje")
    void registroConDatoInvalidoResponde400() throws Exception {
        // Arrange
        when(libroService.registrar(any(Libro.class)))
                .thenThrow(new DatoInvalidoException("El ISBN es obligatorio"));

        // Act + Assert
        mockMvc.perform(post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Clean Code\",\"autor\":\"Robert Martin\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El ISBN es obligatorio"));
    }

    @Test
    @DisplayName("GET /api/libros/{id} de un libro inexistente responde 404")
    void libroNoEncontradoResponde404() throws Exception {
        // Arrange
        when(libroService.buscarPorId(99L)).thenThrow(new LibroNoEncontradoException());

        // Act + Assert
        mockMvc.perform(get("/api/libros/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Libro no existe"));
    }

    @Test
    @DisplayName("GET /api/libros/{id} de un libro existente responde 200")
    void libroEncontradoResponde200() throws Exception {
        // Arrange
        when(libroService.buscarPorId(1L))
                .thenReturn(new Libro("ISBN-1", "Clean Code", "Robert Martin", 2));

        // Act + Assert
        mockMvc.perform(get("/api/libros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Clean Code"));
    }
}
