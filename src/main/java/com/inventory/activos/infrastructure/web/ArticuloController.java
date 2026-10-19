package com.inventory.activos.infrastructure.web;

import com.inventory.activos.application.port.ArticuloCommand;
import com.inventory.activos.application.port.ArticuloUseCase;
import com.inventory.activos.domain.Articulo;
import com.inventory.activos.domain.DepreciacionArticulo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Artículos", description = "Gestión de artículos del inventario y su depreciación")
@RequestMapping("/api/articulos")
public class ArticuloController {

    private final ArticuloUseCase articulos;

    public ArticuloController(ArticuloUseCase articulos) {
        this.articulos = articulos;
    }

    @Operation(summary = "Crear un artículo y calcular su depreciación")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Artículo creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticuloResponse crear(@Valid @RequestBody ArticuloRequest request) {
        return aRespuesta(articulos.crear(aComando(request)));
    }

    @Operation(summary = "Actualizar un artículo", description = "Recalcula la depreciación solo si cambian el valor o la fecha de compra")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artículo actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Artículo no encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Artículo dado de baja", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))})
    @PutMapping("/{id}")
    public ArticuloResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ArticuloRequest request) {
        return aRespuesta(articulos.actualizar(id, aComando(request)));
    }

    @Operation(summary = "Dar de baja un artículo (baja lógica)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Artículo dado de baja"),
            @ApiResponse(responseCode = "404", description = "Artículo no encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@PathVariable Integer id) {
        articulos.darDeBaja(id);
    }

    @Operation(summary = "Obtener un artículo por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artículo encontrado"),
            @ApiResponse(responseCode = "404", description = "Artículo no encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))})
    @GetMapping("/{id}")
    public ArticuloResponse obtener(@PathVariable Integer id) {
        return aRespuesta(articulos.obtenerPorId(id));
    }

    @Operation(summary = "Listar todos los artículos")
    @GetMapping
    public List<ArticuloResponse> listar() {
        return articulos.listar().stream().map(ArticuloController::aRespuesta).toList();
    }

    @Operation(summary = "Obtener un artículo con sus depreciaciones anuales")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artículo con sus depreciaciones ordenadas por año"),
            @ApiResponse(responseCode = "404", description = "Artículo no encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))})
    @GetMapping("/{id}/depreciaciones")
    public ArticuloDepreciacionesResponse depreciaciones(@PathVariable Integer id) {
        return aRespuestaConDepreciaciones(articulos.obtenerConDepreciaciones(id));
    }

    private static ArticuloCommand aComando(ArticuloRequest r) {
        return new ArticuloCommand(r.nombre(), r.serial(), r.descripcion(), r.fechaCompra(), r.valorCompra());
    }

    private static ArticuloResponse aRespuesta(Articulo a) {
        return new ArticuloResponse(a.getIdArticulo(), a.getNombre(), a.getSerial(), a.getDescripcion(),
                a.getFechaCompra(), a.getValorCompra(), a.isActivo());
    }

    private static ArticuloDepreciacionesResponse aRespuestaConDepreciaciones(Articulo articulo) {
        return new ArticuloDepreciacionesResponse(articulo.getIdArticulo(), articulo.getNombre(), articulo.getSerial(), articulo.getDescripcion(),
                articulo.getFechaCompra(), articulo.getValorCompra(), articulo.isActivo(),
                articulo.getDepreciaciones().stream().map(ArticuloController::aRespuesta).toList());
    }

    private static DepreciacionResponse aRespuesta(DepreciacionArticulo depreciacionArticulo) {
        return new DepreciacionResponse(depreciacionArticulo.ano(), depreciacionArticulo.valor(), depreciacionArticulo.porcentaje(),
                depreciacionArticulo.vrActualArticulo());
    }
}
