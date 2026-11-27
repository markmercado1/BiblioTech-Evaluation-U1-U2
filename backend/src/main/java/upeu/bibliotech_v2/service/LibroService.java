package upeu.bibliotech_v2.service;

import org.springframework.stereotype.Service;
import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.exception.LibroNoEncontradoException;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.repository.LibroRepository;
import upeu.bibliotech_v2.validation.LibroValidator;

import java.util.List;

@Service
public class LibroService {

    private final LibroRepository libroRepository;

    public LibroService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    public Libro buscarPorId(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(LibroNoEncontradoException::new);
    }

    /** RN-01: valida los datos, exige ISBN único y guarda el libro. */
    public Libro registrar(Libro libro) {
        LibroValidator.validar(libro);

        if (libroRepository.existsByIsbn(libro.getIsbn())) {
            throw new DatoInvalidoException("El ISBN ya está registrado");
        }

        return libroRepository.save(libro);
    }
}
