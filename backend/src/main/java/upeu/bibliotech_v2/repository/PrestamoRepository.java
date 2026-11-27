package upeu.bibliotech_v2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import upeu.bibliotech_v2.model.Prestamo;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    long countByEstudianteIdAndEstado(Long estudianteId, String estado);

    List<Prestamo> findByEstudianteIdAndEstado(Long estudianteId, String estado);
}
