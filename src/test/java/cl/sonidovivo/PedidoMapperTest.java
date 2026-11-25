package cl.sonidovivo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PedidoMapperTest {
  @Test void devuelveLaEntregaGuardadaEnElPedido() {
    Usuario cliente = new Usuario("Ana Perfil", "ana@sonidovivo.cl", "hash", Rol.CLIENTE);
    cliente.direccion = "Dirección del perfil";
    cliente.comuna = "Quilpué";
    Pedido pedido = new Pedido();
    pedido.id = 1L;
    pedido.codigoPedido = "SV-PRUEBA";
    pedido.cliente = cliente;
    pedido.entregaNombre = "Ana Recepción";
    pedido.entregaCorreo = "recepcion@sonidovivo.cl";
    pedido.entregaDireccion = "Calle Marina 123";
    pedido.entregaComuna = "Viña del Mar";
    pedido.total = BigDecimal.TEN;

    PedidoResponse respuesta = Mapper.pedido(pedido);

    assertEquals("Ana Recepción", respuesta.entrega().nombre());
    assertEquals("recepcion@sonidovivo.cl", respuesta.entrega().correo());
    assertEquals("Calle Marina 123", respuesta.entrega().direccion());
    assertEquals("Viña del Mar", respuesta.entrega().comuna());
  }

  @Test void usaLosDatosDelClienteParaPedidosAnterioresSinEntregaGuardada() {
    Usuario cliente = new Usuario("Ana Perfil", "ana@sonidovivo.cl", "hash", Rol.CLIENTE);
    cliente.direccion = "Dirección del perfil";
    cliente.comuna = "Quilpué";
    Pedido pedido = new Pedido();
    pedido.id = 2L;
    pedido.codigoPedido = "SV-ANTERIOR";
    pedido.cliente = cliente;
    pedido.total = BigDecimal.TEN;

    EntregaPedidoResponse entrega = Mapper.pedido(pedido).entrega();

    assertEquals("Ana Perfil", entrega.nombre());
    assertEquals("ana@sonidovivo.cl", entrega.correo());
    assertEquals("Dirección del perfil", entrega.direccion());
    assertEquals("Quilpué", entrega.comuna());
  }
}