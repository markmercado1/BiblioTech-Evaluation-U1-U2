package upeu.bibliotech_v2.model;
import jakarta.persistence.*;

@Entity
@Table(name = "estudiantes")
public class Estudiante {

    public static final String ACTIVO = "ACTIVO";
    public static final String INACTIVO = "INACTIVO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String dni;

    private String nombre;
    private String estado;

    public Estudiante() {
    }

    public Estudiante(String nombre, String estado) {
        this.nombre = nombre;
        this.estado = estado;
    }

    public Estudiante(String dni, String nombre, String estado) {
        this(nombre, estado);
        this.dni = dni;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
