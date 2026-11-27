package upeu.bibliotech_v2.service;

import org.springframework.stereotype.Service;
import upeu.bibliotech_v2.exception.EstudianteNoEncontradoException;
import upeu.bibliotech_v2.exception.LibroNoEncontradoException;
import upeu.bibliotech_v2.exception.PrestamoNoEncontradoException;
import upeu.bibliotech_v2.exception.PrestamoNoPermitidoException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.model.Prestamo;
import upeu.bibliotech_v2.repository.EstudianteRepository;
import upeu.bibliotech_v2.repository.LibroRepository;
import upeu.bibliotech_v2.repository.PrestamoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PrestamoService {

    private static final String ESTADO_ACTIVO = "ACTIVO";
    private static final String ESTADO_DEVUELTO = "DEVUELTO";
    private static final int MAXIMO_PRESTAMOS_ACTIVOS = 3;

    private final EstudianteRepository estudianteRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    public PrestamoService(
            EstudianteRepository estudianteRepository,
            LibroRepository libroRepository,
            PrestamoRepository prestamoRepository) {

        this.estudianteRepository = estudianteRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
    }

    public boolean puedePrestar(Long estudianteId, Long libroId) {

        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() ->
                        new RuntimeException("Estudiante no existe"));

        if (!"ACTIVO".equalsIgnoreCase(estudiante.getEstado())) {
            return false;
        }

        long prestamosActivos =
                prestamoRepository.countByEstudianteIdAndEstado(
                        estudianteId, "ACTIVO");

        if (prestamosActivos >= 3) {
            return false;
        }

        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() ->
                        new RuntimeException("Libro no existe"));

        if (libro.getEjemplaresDisponibles() <= 0) {
            return false;
        }

        return true;
    }

    /**
     * Devuelve los préstamos con estado ACTIVO de un estudiante.
     * Lanza EstudianteNoEncontradoException si el estudiante no existe.
     */
    public List<Prestamo> listarPrestamosActivos(Long estudianteId) {

        if (!estudianteRepository.existsById(estudianteId)) {
            throw new EstudianteNoEncontradoException();
        }

        return prestamoRepository.findByEstudianteIdAndEstado(
                estudianteId, "ACTIVO");
    }

    /**
     * RN-03: registra un préstamo. Las condiciones se evalúan en este orden:
     * D1 el libro existe, D2 tiene ejemplares, D3 el estudiante está ACTIVO,
     * D4 tiene menos de 3 préstamos activos. Al prestar baja un ejemplar.
     */
    public Prestamo registrarPrestamo(Long estudianteId, Long libroId) {

        Optional<Libro> libroEncontrado = libroRepository.findById(libroId);
        if (libroEncontrado.isEmpty()) {                                        // D1
            throw new LibroNoEncontradoException();
        }
        Libro libro = libroEncontrado.get();

        if (libro.getEjemplaresDisponibles() <= 0) {                            // D2
            throw new PrestamoNoPermitidoException(
                    "El libro no tiene ejemplares disponibles");
        }

        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(EstudianteNoEncontradoException::new);

        if (!Estudiante.ACTIVO.equalsIgnoreCase(estudiante.getEstado())) {      // D3
            throw new PrestamoNoPermitidoException("El estudiante no está activo");
        }

        long prestamosActivos = prestamoRepository
                .countByEstudianteIdAndEstado(estudianteId, ESTADO_ACTIVO);
        if (prestamosActivos >= MAXIMO_PRESTAMOS_ACTIVOS) {                     // D4
            throw new PrestamoNoPermitidoException(
                    "El estudiante alcanzó el máximo de 3 préstamos activos");
        }

        Prestamo prestamo = new Prestamo(
                estudiante,
                libro,
                LocalDate.now(),
                ESTADO_ACTIVO
        );

        libro.setEjemplaresDisponibles(
                libro.getEjemplaresDisponibles() - 1
        );

        libroRepository.save(libro);

        return prestamoRepository.save(prestamo);
    }

    /**
     * RN-04: devuelve un libro. D1 el préstamo existe, D2 está ACTIVO.
     * Al devolver sube un ejemplar y el préstamo pasa a DEVUELTO.
     */
    public Prestamo devolverLibro(Long prestamoId) {

        Optional<Prestamo> prestamoEncontrado = prestamoRepository.findById(prestamoId);
        if (prestamoEncontrado.isEmpty()) {                                     // D1
            throw new PrestamoNoEncontradoException();
        }
        Prestamo prestamo = prestamoEncontrado.get();

        if (!ESTADO_ACTIVO.equals(prestamo.getEstado())) {                      // D2
            throw new PrestamoNoPermitidoException("El préstamo ya fue devuelto");
        }

        Libro libro = prestamo.getLibro();
        libro.setEjemplaresDisponibles(
                libro.getEjemplaresDisponibles() + 1
        );
        prestamo.setEstado(ESTADO_DEVUELTO);

        libroRepository.save(libro);

        return prestamoRepository.save(prestamo);
    }

}
