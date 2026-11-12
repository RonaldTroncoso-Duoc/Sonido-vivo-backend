package cl.sonidovivo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

class NotFoundException extends RuntimeException { NotFoundException(String message) { super(message); } }
class BadRequestException extends RuntimeException { BadRequestException(String message) { super(message); } }
class UnauthorizedException extends RuntimeException { UnauthorizedException(String message) { super(message); } }

@Service @Transactional class AuthService {
  private final UsuarioRepository usuarios; private final PasswordEncoder encoder; private final JwtService jwt;
  AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) { this.usuarios = usuarios; this.encoder = encoder; this.jwt = jwt; }
  AuthResponse login(LoginRequest request) { Usuario usuario = usuarios.findByEmailIgnoreCase(request.email()).orElseThrow(() -> new UnauthorizedException("Correo o contraseña incorrectos")); if (!usuario.activo || !encoder.matches(request.password(), usuario.password)) throw new UnauthorizedException("Correo o contraseña incorrectos"); return new AuthResponse(jwt.generar(usuario), Mapper.usuario(usuario)); }
  UsuarioResponse registro(RegistroRequest request) { if (usuarios.existsByEmailIgnoreCase(request.email())) throw new BadRequestException("Ya existe una cuenta con ese correo"); Usuario usuario = new Usuario(request.nombre().trim(), request.email().trim().toLowerCase(), encoder.encode(request.password()), Rol.CLIENTE); usuario.telefono = request.telefono().trim(); usuario.direccion = request.direccion().trim(); usuario.comuna = request.comuna().trim(); usuario.region = request.region().trim(); return Mapper.usuario(usuarios.save(usuario)); }
}

@Service @Transactional class ProductoService {
  private final ProductoRepository productos;
  ProductoService(ProductoRepository productos) { this.productos = productos; }
  @Transactional(readOnly = true) Page<ProductoResponse> listar(String busqueda, String categoria, String stock, String orden, int pagina, int tamanio) { String campo = switch (orden == null ? "" : orden) { case "precio", "precioDesc" -> "precio"; case "nombre" -> "nombre"; default -> "fechaCreacion"; }; Sort.Direction direccion = "precioDesc".equals(orden) ? Sort.Direction.DESC : Sort.Direction.ASC; return productos.buscar(vacioANull(busqueda), vacioANull(categoria), vacioANull(stock), PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanio, 1), 100), Sort.by(direccion, campo))).map(Mapper::producto); }
  @Transactional(readOnly = true) ProductoResponse uno(Long id) { return Mapper.producto(producto(id)); }
  ProductoResponse crear(ProductoRequest request) { if (productos.existsByCodigoIgnoreCase(request.codigo())) throw new BadRequestException("El código de producto ya existe"); Producto producto = new Producto(); copiar(request, producto); return Mapper.producto(productos.save(producto)); }
  ProductoResponse actualizar(Long id, ProductoRequest request) { Producto producto = producto(id); if (!producto.codigo.equalsIgnoreCase(request.codigo()) && productos.existsByCodigoIgnoreCase(request.codigo())) throw new BadRequestException("El código de producto ya existe"); copiar(request, producto); return Mapper.producto(producto); }
  void eliminar(Long id) { productos.delete(producto(id)); }
  Producto producto(Long id) { return productos.findById(id).orElseThrow(() -> new NotFoundException("Producto no encontrado")); }
  private void copiar(ProductoRequest request, Producto producto) { producto.codigo = request.codigo().trim().toUpperCase(); producto.nombre = request.nombre().trim(); producto.categoria = request.categoria().trim(); producto.marca = request.marca(); producto.modelo = request.modelo(); producto.descripcion = request.descripcion(); producto.precio = request.precio(); producto.stock = request.stock(); producto.imagen = request.imagen(); producto.oferta = request.oferta(); }
  private String vacioANull(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
}

@Service @Transactional class PedidoService {
  private final PedidoRepository pedidos; private final ProductoService productos; private final UsuarioRepository usuarios;
  PedidoService(PedidoRepository pedidos, ProductoService productos, UsuarioRepository usuarios) { this.pedidos = pedidos; this.productos = productos; this.usuarios = usuarios; }
  @Transactional(readOnly = true) public List<PedidoResponse> listarCliente(String email) { return pedidos.findByClienteIdOrderByFechaPedidoDesc(cliente(email).id).stream().map(Mapper::pedido).toList(); }
  @Transactional(readOnly = true) public List<PedidoResponse> listarGestion() { return pedidos.findAll().stream().sorted(Comparator.comparing((Pedido pedido) -> pedido.fechaPedido).reversed()).map(Mapper::pedido).toList(); }
  @Transactional(readOnly = true) public PedidoResponse detalle(Long id, String email, boolean operador) { Pedido pedido = pedido(id); if (!operador && !pedido.cliente.email.equalsIgnoreCase(email)) throw new AccessDeniedException("No puede consultar este pedido"); return Mapper.pedido(pedido); }
  @Transactional(readOnly = true) public PedidoResponse seguimiento(SeguimientoPedidoRequest request) { return pedidos.buscarSeguimiento(request.codigoPedido().trim(),request.correo().trim()).map(Mapper::pedido).orElseThrow(() -> new NotFoundException("No se encontró un pedido con esos datos")); }
  public PedidoResponse crear(String email, PedidoRequest request) { Pedido pedido = new Pedido(); pedido.cliente = cliente(email); pedido.codigoPedido = "SV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(); pedido.total = BigDecimal.ZERO; for (ItemPedidoRequest item : request.items()) { Producto producto = productos.producto(item.productoId()); if (producto.stock < item.cantidad()) throw new BadRequestException("Stock insuficiente para " + producto.nombre); producto.stock -= item.cantidad(); ItemPedido detalle = new ItemPedido(); detalle.pedido = pedido; detalle.producto = producto; detalle.cantidad = item.cantidad(); detalle.precioUnitario = producto.precio; pedido.items.add(detalle); pedido.total = pedido.total.add(producto.precio.multiply(BigDecimal.valueOf(item.cantidad()))); } HistorialEstado historial = new HistorialEstado(); historial.pedido = pedido; historial.estado = EstadoPedido.Confirmado; pedido.historial.add(historial); return Mapper.pedido(pedidos.save(pedido)); }
  public PedidoResponse estado(Long id, CambioEstadoRequest request) { Pedido pedido = pedido(id); if (request.estado().ordinal() < pedido.estado.ordinal() || request.estado().ordinal() > pedido.estado.ordinal() + 1) throw new BadRequestException("El pedido solo puede avanzar al siguiente estado"); if (request.estado() == pedido.estado) return Mapper.pedido(pedido); pedido.estado = request.estado(); pedido.fechaActualizacion = Instant.now(); HistorialEstado historial = new HistorialEstado(); historial.pedido = pedido; historial.estado = request.estado(); pedido.historial.add(historial); return Mapper.pedido(pedido); }
  private Pedido pedido(Long id) { return pedidos.findById(id).orElseThrow(() -> new NotFoundException("Pedido no encontrado")); }
  private Usuario cliente(String email) { return usuarios.findByEmailIgnoreCase(email).orElseThrow(() -> new NotFoundException("Usuario no encontrado")); }
}

@Service @Transactional class UsuarioService {
  private final UsuarioRepository usuarios;
  UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) { this.usuarios = usuarios; }
  @Transactional(readOnly = true) List<UsuarioResponse> listar() { return usuarios.findAll().stream().map(Mapper::usuario).toList(); }
  @Transactional(readOnly = true) UsuarioResponse uno(Long id) { return Mapper.usuario(entidad(id)); }
  UsuarioResponse actualizar(Long id, UsuarioRequest request) { Usuario usuario = entidad(id); if (!usuario.email.equalsIgnoreCase(request.email()) && usuarios.existsByEmailIgnoreCase(request.email())) throw new BadRequestException("El correo ya está en uso"); usuario.nombre = request.nombre().trim(); usuario.email = request.email().trim().toLowerCase(); usuario.username = usuario.email; usuario.telefono = request.telefono(); usuario.direccion = request.direccion(); usuario.comuna = request.comuna(); usuario.region = request.region(); if (request.rol() != null) usuario.rol = request.rol(); if (request.activo() != null) usuario.activo = request.activo(); return Mapper.usuario(usuario); }
  void eliminar(Long id) { usuarios.delete(entidad(id)); }
  private Usuario entidad(Long id) { return usuarios.findById(id).orElseThrow(() -> new NotFoundException("Usuario no encontrado")); }
}

@Service @Transactional class MensajeService {
  private final MensajeContactoRepository contactos; private final SolicitudReparacionRepository reparaciones;
  MensajeService(MensajeContactoRepository contactos, SolicitudReparacionRepository reparaciones) { this.contactos = contactos; this.reparaciones = reparaciones; }
  MensajeResponse contacto(ContactoRequest request) { MensajeContacto mensaje = new MensajeContacto(); mensaje.nombre = request.nombre().trim(); mensaje.correo = request.correo().trim().toLowerCase(); mensaje.mensaje = request.mensaje().trim(); return Mapper.contacto(contactos.save(mensaje)); }
  MensajeResponse reparacion(ReparacionRequest request) { SolicitudReparacion solicitud = new SolicitudReparacion(); solicitud.nombre = request.nombre().trim(); solicitud.correo = request.correo().trim().toLowerCase(); solicitud.telefono = request.telefono().trim(); solicitud.tipoInstrumento = request.tipoInstrumento().trim(); solicitud.marcaModelo = request.marcaModelo(); solicitud.servicio = request.servicio().trim(); solicitud.descripcion = request.descripcion().trim(); solicitud.entrega = request.entrega(); return Mapper.reparacion(reparaciones.save(solicitud)); }
  @Transactional(readOnly = true) List<MensajeResponse> listar() { List<MensajeResponse> salida = new ArrayList<>(); contactos.findAll().forEach(mensaje -> salida.add(Mapper.contacto(mensaje))); reparaciones.findAll().forEach(solicitud -> salida.add(Mapper.reparacion(solicitud))); salida.sort(Comparator.comparing(MensajeResponse::fecha).reversed()); return salida; }
  MensajeResponse responder(String tipo, Long id, RespuestaMensajeRequest request) { Instant ahora = Instant.now(); if ("contacto".equalsIgnoreCase(tipo)) { MensajeContacto mensaje = contactos.findById(id).orElseThrow(() -> new NotFoundException("Mensaje no encontrado")); mensaje.respuesta = request.respuesta().trim(); mensaje.fechaRespuesta = ahora; mensaje.estado = "RESPONDIDO"; return Mapper.contacto(mensaje); } if ("reparacion".equalsIgnoreCase(tipo)) { SolicitudReparacion solicitud = reparaciones.findById(id).orElseThrow(() -> new NotFoundException("Solicitud de reparación no encontrada")); solicitud.respuesta = request.respuesta().trim(); solicitud.fechaRespuesta = ahora; solicitud.estado = "RESPONDIDA"; return Mapper.reparacion(solicitud); } throw new BadRequestException("Tipo de mensaje no válido"); }
}

class Mapper {
  static UsuarioResponse usuario(Usuario usuario) { return new UsuarioResponse(usuario.id, usuario.username, usuario.nombre, usuario.email, usuario.rol, usuario.activo, usuario.telefono, usuario.direccion, usuario.comuna, usuario.region, usuario.fechaCreacion); }
  static ProductoResponse producto(Producto producto) { return new ProductoResponse(producto.id, producto.codigo, producto.nombre, producto.categoria, producto.marca, producto.modelo, producto.descripcion, producto.precio, producto.stock, producto.imagen, producto.oferta, producto.fechaCreacion); }
  static PedidoResponse pedido(Pedido pedido) { return new PedidoResponse(pedido.id, pedido.codigoPedido, usuario(pedido.cliente), pedido.estado, pedido.total, pedido.fechaPedido, pedido.fechaActualizacion, pedido.items.stream().map(item -> new ItemPedidoResponse(item.id, item.producto.id, item.producto.codigo, item.producto.nombre, item.cantidad, item.precioUnitario)).toList(), pedido.historial.stream().map(historial -> new HistorialResponse(historial.estado, historial.fecha)).toList()); }
  static MensajeResponse contacto(MensajeContacto mensaje) { return new MensajeResponse(mensaje.id, "contacto", mensaje.nombre, mensaje.correo, null, null, null, null, null, null, mensaje.mensaje, mensaje.fecha, mensaje.respuesta, mensaje.fechaRespuesta, mensaje.estado); }
  static MensajeResponse reparacion(SolicitudReparacion solicitud) { return new MensajeResponse(solicitud.id, "reparacion", solicitud.nombre, solicitud.correo, solicitud.telefono, solicitud.tipoInstrumento, solicitud.marcaModelo, solicitud.servicio, solicitud.descripcion, solicitud.entrega, null, solicitud.fecha, solicitud.respuesta, solicitud.fechaRespuesta, solicitud.estado); }
}