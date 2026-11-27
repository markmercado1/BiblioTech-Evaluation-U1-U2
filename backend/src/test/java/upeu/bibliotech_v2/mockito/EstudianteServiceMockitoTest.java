package upeu.bibliotech_v2.mockito;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.repository.EstudianteRepository;
import upeu.bibliotech_v2.service.EstudianteService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** EstudianteService con el repositorio simulado: RN-02 (DNI único y estado inicial ACTIVO). */
@ExtendWith(MockitoExtension.class)
class EstudianteServiceMockitoTest {

    @Mock
    private EstudianteRepository estudianteRepository;

    @InjectMocks
    private EstudianteService estudianteService;

    @Test
    @DisplayName("Registrar estudiante válido: queda ACTIVO y se guarda")
    void registrarEstudianteQuedaActivo() {
        // Arrange
        Estudiante estudiante = new Estudiante("70000001", "Ana", "INACTIVO");
        when(estudianteRepository.existsByDni("70000001")).thenReturn(false);
        when(estudianteRepository.save(estudiante)).thenReturn(estudiante);

        // Act
        Estudiante resultado = estudianteService.registrar(estudiante);

        // Assert
        assertEquals("ACTIVO", resultado.getEstado());
        verify(estudianteRepository).save(estudiante);
    }

    @Test
    @DisplayName("Registrar estudiante con DNI repetido: se rechaza y no se guarda")
    void registrarEstudianteConDniRepetidoNoLoGuarda() {
        // Arrange
        Estudiante estudiante = new Estudiante("70000001", "Ana", null);
        when(estudianteRepository.existsByDni("70000001")).thenReturn(true);

        // Act
        DatoInvalidoException e = assertThrows(DatoInvalidoException.class,
                () -> estudianteService.registrar(estudiante));

        // Assert
        assertEquals("El DNI ya está registrado", e.getMessage());
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar estudiante sin DNI: se rechaza sin consultar ni guardar")
    void registrarEstudianteSinDniNoConsultaNiGuarda() {
        // Arrange
        Estudiante estudiante = new Estudiante("", "Ana", null);

        // Act + Assert
        assertThrows(DatoInvalidoException.class, () -> estudianteService.registrar(estudiante));
        verify(estudianteRepository, never()).existsByDni(any());
        verify(estudianteRepository, never()).save(any());
    }
}
