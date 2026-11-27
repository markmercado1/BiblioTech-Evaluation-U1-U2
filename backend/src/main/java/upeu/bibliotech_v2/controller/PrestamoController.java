package upeu.bibliotech_v2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.bibliotech_v2.model.Prestamo;
import upeu.bibliotech_v2.service.PrestamoService;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {
    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping("/validar")
    public ResponseEntity<Boolean> validarPrestamo(
            @RequestParam Long estudianteId,
            @RequestParam Long libroId) {

        boolean resultado =
                prestamoService.puedePrestar(estudianteId, libroId);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Prestamo>> listarPrestamosActivos(
            @RequestParam Long estudianteId) {

        return ResponseEntity.ok(
                prestamoService.listarPrestamosActivos(estudianteId));
    }

    @PostMapping
    public ResponseEntity<Prestamo> registrarPrestamo(
            @RequestParam Long estudianteId,
            @RequestParam Long libroId) {

        Prestamo prestamo =
                prestamoService.registrarPrestamo(estudianteId, libroId);

        return ResponseEntity.ok(prestamo);
    }

    @PutMapping("/{id}/devolucion")
    public ResponseEntity<Prestamo> devolverLibro(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoService.devolverLibro(id));
    }
}
