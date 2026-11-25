package cl.sonidovivo;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.Instant; import java.util.*;
record LoginRequest(@Email @NotBlank String email,@NotBlank String password){}
record RegistroRequest(@NotBlank @Size(min=3,max=120) String nombre,@Email @NotBlank String email,@NotBlank @Size(min=6,max=100) String password,@NotBlank String telefono,@NotBlank String direccion,@NotBlank String comuna,@NotBlank String region){}
record UsuarioRequest(@NotBlank String nombre,@Email @NotBlank String email,String telefono,String direccion,String comuna,String region,Rol rol,Boolean activo){}
record UsuarioResponse(Long id,String username,String nombre,String email,Rol rol,boolean activo,String telefono,String direccion,String comuna,String region,Instant fechaCreacion){}
record AuthResponse(String token,UsuarioResponse usuario){}
record ProductoRequest(@NotBlank String codigo,@NotBlank String nombre,@NotBlank String categoria,String marca,String modelo,String descripcion,@NotNull @DecimalMin("0.01") BigDecimal precio,@NotNull @Min(0) Integer stock,String imagen,boolean oferta){}
record ProductoResponse(Long id,String codigo,String nombre,String categoria,String marca,String modelo,String descripcion,BigDecimal precio,Integer stock,String imagen,boolean oferta,Instant fechaCreacion){}
record ItemPedidoRequest(@NotNull Long productoId,@NotNull @Min(1) Integer cantidad){}
record EntregaPedidoRequest(@NotBlank @Size(min=3,max=120) String nombre,@Email @NotBlank String correo,@NotBlank @Size(min=5,max=255) String direccion,@NotBlank @Size(min=2,max=120) String comuna){}
record PedidoRequest(@NotEmpty List<@Valid ItemPedidoRequest> items,@NotNull @Valid EntregaPedidoRequest entrega){}
record ItemPedidoResponse(Long id,Long productoId,String codigoProducto,String nombreProducto,Integer cantidad,BigDecimal precioUnitario){}
record HistorialResponse(EstadoPedido estado,Instant fecha){}
record EntregaPedidoResponse(String nombre,String correo,String direccion,String comuna){}
record PedidoResponse(Long id,String codigoPedido,UsuarioResponse cliente,EntregaPedidoResponse entrega,EstadoPedido estado,BigDecimal total,Instant fechaPedido,Instant fechaActualizacion,List<ItemPedidoResponse> items,List<HistorialResponse> historial){}
record CambioEstadoRequest(@NotNull EstadoPedido estado){}
record SeguimientoPedidoRequest(@NotBlank String codigoPedido,@Email @NotBlank String correo){}
record ContactoRequest(@NotBlank @Size(min=3) String nombre,@Email @NotBlank String correo,@NotBlank @Size(min=5,max=3000) String mensaje){}
record ReparacionRequest(@NotBlank String nombre,@Email @NotBlank String correo,@NotBlank String telefono,@NotBlank String tipoInstrumento,String marcaModelo,@NotBlank String servicio,@NotBlank @Size(min=5,max=3000) String descripcion,String entrega){}
record MensajeResponse(Long id,String tipo,String nombre,String correo,String telefono,String tipoInstrumento,String marcaModelo,String servicio,String descripcion,String entrega,String mensaje,Instant fecha,String respuesta,Instant fechaRespuesta,String estado){}
record RespuestaMensajeRequest(@NotBlank @Size(min=5,max=3000) String respuesta){}