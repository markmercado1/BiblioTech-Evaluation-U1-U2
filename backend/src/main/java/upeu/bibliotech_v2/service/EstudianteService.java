package upeu.bibliotech_v2.service;

import org.springframework.stereotype.Service;
import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.repository.EstudianteRepository;
import upeu.bibliotech_v2.validation.EstudianteValidator;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    /** RN-02: valida los datos, exige DNI único y guarda al estudiante como ACTIVO. */
    public Estudiante registrar(Estudiante estudiante) {
        EstudianteValidator.validar(estudiante);

        if (estudianteRepository.existsByDni(estudiante.getDni())) {
            throw new DatoInvalidoException("El DNI ya está registrado");
        }

        estudiante.setEstado(Estudiante.ACTIVO);
        return estudianteRepository.save(estudiante);
    }
}
