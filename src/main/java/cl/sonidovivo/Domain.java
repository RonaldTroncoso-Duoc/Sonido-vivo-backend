package cl.sonidovivo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

enum Rol { ADMIN, VENDEDOR, CLIENTE }
enum EstadoPedido { Confirmado, Preparacion, Despacho, Entregado }

@Entity @Table(name="usuarios") class Usuario {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  @Column(nullable=false, unique=true, length=80) String username;
  @Column(nullable=false, unique=true, length=120) String email;
  @Column(nullable=false) String password;
  @Enumerated(EnumType.STRING) @Column(nullable=false) Rol rol;
  boolean activo=true; @Column(nullable=false) String nombre; String telefono; String direccion; String comuna; String region;
  @Column(nullable=false, updatable=false) Instant fechaCreacion=Instant.now();
  protected Usuario(){} Usuario(String nombre,String email,String password,Rol rol){this.nombre=nombre;this.email=email;this.username=email;this.password=password;this.rol=rol;}
}
@Entity @Table(name="productos") class Producto {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  @Column(nullable=false,unique=true,length=50) String codigo; @Column(nullable=false) String nombre; @Column(nullable=false) String categoria; String marca; String modelo;
  @Column(length=3000) String descripcion; @Column(nullable=false,precision=14,scale=2) BigDecimal precio; @Column(nullable=false) Integer stock; String imagen; boolean oferta;
  @Column(nullable=false,updatable=false) Instant fechaCreacion=Instant.now(); protected Producto(){}
}
@Entity @Table(name="pedidos") class Pedido {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @Column(nullable=false,unique=true) String codigoPedido;
  @ManyToOne(optional=false,fetch=FetchType.LAZY) Usuario cliente; @Enumerated(EnumType.STRING) @Column(nullable=false) EstadoPedido estado=EstadoPedido.Confirmado;
  @Column(nullable=false,precision=14,scale=2) BigDecimal total; @Column(nullable=false) Instant fechaPedido=Instant.now(); @Column(nullable=false) Instant fechaActualizacion=Instant.now();
  @OneToMany(mappedBy="pedido",cascade=CascadeType.ALL,orphanRemoval=true) List<ItemPedido> items=new ArrayList<>();
  @OneToMany(mappedBy="pedido",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("fecha ASC") List<HistorialEstado> historial=new ArrayList<>(); protected Pedido(){}
}
@Entity @Table(name="items_pedido") class ItemPedido { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @ManyToOne(optional=false) Pedido pedido; @ManyToOne(optional=false) Producto producto; @Column(nullable=false) Integer cantidad; @Column(nullable=false,precision=14,scale=2) BigDecimal precioUnitario; protected ItemPedido(){} }
@Entity @Table(name="historial_estados") class HistorialEstado { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @ManyToOne(optional=false) Pedido pedido; @Enumerated(EnumType.STRING) @Column(nullable=false) EstadoPedido estado; @Column(nullable=false) Instant fecha=Instant.now(); protected HistorialEstado(){} }
@Entity @Table(name="mensajes_contacto") class MensajeContacto { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @Column(nullable=false) String nombre; @Column(nullable=false) String correo; @Column(nullable=false,length=3000) String mensaje; @Column(nullable=false) Instant fecha=Instant.now(); @Column(length=3000) String respuesta; Instant fechaRespuesta; @Column(nullable=false) String estado="RECIBIDO"; protected MensajeContacto(){} }
@Entity @Table(name="solicitudes_reparacion") class SolicitudReparacion { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @Column(nullable=false) String nombre; @Column(nullable=false) String correo; @Column(nullable=false) String telefono; @Column(nullable=false) String tipoInstrumento; String marcaModelo; @Column(nullable=false) String servicio; @Column(nullable=false,length=3000) String descripcion; String entrega; @Column(nullable=false) Instant fecha=Instant.now(); @Column(length=3000) String respuesta; Instant fechaRespuesta; @Column(nullable=false) String estado="RECIBIDA"; protected SolicitudReparacion(){} }