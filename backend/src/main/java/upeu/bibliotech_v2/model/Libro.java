package upeu.bibliotech_v2.model;
import jakarta.persistence.*;

@Entity
@Table(name = "libros")
public class Libro {

    public static final String DISPONIBLE = "DISPONIBLE";
    public static final String NO_DISPONIBLE = "NO DISPONIBLE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String isbn;

    private String titulo;
    private String autor;

    private int ejemplaresDisponibles;

    public Libro() {
    }

    public Libro(String titulo, String autor, int ejemplaresDisponibles) {
        this.titulo = titulo;
        this.autor = autor;
        this.ejemplaresDisponibles = ejemplaresDisponibles;
    }

    public Libro(String isbn, String titulo, String autor, int ejemplaresDisponibles) {
        this(titulo, autor, ejemplaresDisponibles);
        this.isbn = isbn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public int getEjemplaresDisponibles() {
        return ejemplaresDisponibles;
    }

    public void setEjemplaresDisponibles(int ejemplaresDisponibles) {
        this.ejemplaresDisponibles = ejemplaresDisponibles;
    }

    /** El estado no se guarda: se calcula a partir de los ejemplares (CA-06 y CA-07). */
    public String getEstado() {
        return ejemplaresDisponibles > 0 ? DISPONIBLE : NO_DISPONIBLE;
    }
}
