package cl.sonidovivo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {
  @Mock PedidoRepository pedidos;
  @Mock ProductoService productos;
  @Mock UsuarioRepository usuarios;

  @Test void cargaElClienteAsociadoParaLaBandejaDeGestion() {
    Usuario cliente = new Usuario("Diana", "diana@sonidovivo.cl", "hash", Rol.CLIENTE);
    cliente.id = 3L;
    cliente.telefono = "999999999";
    Pedido pedido = new Pedido();
    pedido.id = 1L;
    pedido.codigoPedido = "SV-PRUEBA";
    pedido.cliente = cliente;
    pedido.entregaDireccion = "Alameda 3366";
    pedido.entregaComuna = "Santiago Centro";
    pedido.total = java.math.BigDecimal.TEN;
    when(pedidos.listarGestion()).thenReturn(List.of(pedido));

    PedidoResponse respuesta = new PedidoService(pedidos, productos, usuarios).listarGestion().getFirst();

    verify(pedidos).listarGestion();
    assertEquals("Diana", respuesta.cliente().nombre());
    assertEquals("999999999", respuesta.cliente().telefono());
    assertEquals("Alameda 3366", respuesta.entrega().direccion());
  }
}