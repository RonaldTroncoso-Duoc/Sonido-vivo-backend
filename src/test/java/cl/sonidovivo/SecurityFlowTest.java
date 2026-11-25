package cl.sonidovivo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {ProductoController.class, MensajeController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityFlowTest {
  @Autowired MockMvc mvc;
  @MockBean JwtService jwt;
  @MockBean UsuarioRepository usuarios;
  @MockBean ProductoService productos;
  @MockBean MensajeService mensajes;

  private final String producto = "{\"codigo\":\"GA999\",\"nombre\":\"Guitarra prueba\",\"categoria\":\"Cuerdas\",\"precio\":1000,\"stock\":2,\"oferta\":false}";

  private void autenticar(String token, Rol rol) {
    Usuario usuario = new Usuario("Prueba", "prueba@sonidovivo.cl", "hash", rol);
    when(jwt.email(token)).thenReturn("prueba@sonidovivo.cl");
    when(usuarios.findByEmailIgnoreCase("prueba@sonidovivo.cl")).thenReturn(Optional.of(usuario));
  }

  @Test void crearProductoSinTokenEs401() throws Exception {
    mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(producto)).andExpect(status().isUnauthorized());
  }

  @Test void crearProductoConClienteEs403() throws Exception {
    autenticar("token-cliente", Rol.CLIENTE);
    mvc.perform(post("/api/productos").header("Authorization", "Bearer token-cliente").contentType(MediaType.APPLICATION_JSON).content(producto)).andExpect(status().isForbidden());
  }

  @Test void crearProductoConAdminEs201() throws Exception {
    autenticar("token-admin", Rol.ADMIN);
    when(productos.crear(any())).thenReturn(new ProductoResponse(1L, "GA999", "Guitarra prueba", "Cuerdas", null, null, null, java.math.BigDecimal.valueOf(1000), 2, null, false, java.time.Instant.now()));
    mvc.perform(post("/api/productos").header("Authorization", "Bearer token-admin").contentType(MediaType.APPLICATION_JSON).content(producto)).andExpect(status().isCreated());
  }

  @Test void mensajesPropiosConClienteEs200() throws Exception {
    autenticar("token-cliente", Rol.CLIENTE);
    mvc.perform(get("/api/mensajes/mios").header("Authorization", "Bearer token-cliente")).andExpect(status().isOk());
  }

  @Test void mensajesPropiosSinTokenEs401() throws Exception {
    mvc.perform(get("/api/mensajes/mios")).andExpect(status().isUnauthorized());
  }
}