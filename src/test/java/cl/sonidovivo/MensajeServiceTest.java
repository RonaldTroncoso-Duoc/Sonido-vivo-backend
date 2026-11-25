package cl.sonidovivo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MensajeServiceTest {
  @Mock MensajeContactoRepository contactos;
  @Mock SolicitudReparacionRepository reparaciones;

  @Test void noPermiteSobrescribirUnaRespuestaYaEnviada() {
    MensajeContacto mensaje = new MensajeContacto();
    mensaje.id = 1L;
    mensaje.respuesta = "Respuesta original";
    when(contactos.findById(1L)).thenReturn(Optional.of(mensaje));

    MensajeService service = new MensajeService(contactos, reparaciones);
    BadRequestException error = assertThrows(BadRequestException.class, () -> service.responder("contacto", 1L, new RespuestaMensajeRequest("Respuesta modificada")));

    assertEquals("Este mensaje ya tiene una respuesta enviada y no puede modificarse", error.getMessage());
    assertEquals("Respuesta original", mensaje.respuesta);
  }
}