# Sonido Vivo Backend

API REST con Spring Boot 3, Java 21, MySQL 8, JWT, BCrypt y Swagger.

## Ejecución local

### Requisitos

- Java 21 o superior.
- Docker Desktop iniciado para ejecutar MySQL.
- Puerto `3306` disponible para MySQL y puerto `8080` disponible para la API.

### Opción recomendada: MySQL en Docker y backend local

Desde la raíz de este repositorio, abre dos terminales de PowerShell.

**Terminal 1 — MySQL**

```powershell
docker compose up -d mysql
docker compose ps
```

Espera a que el servicio `mysql` figure como `healthy` o `running`.

**Terminal 2 — API Spring Boot**

```powershell
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Swagger está disponible en `http://localhost:8080/swagger-ui.html` y el documento OpenAPI en `http://localhost:8080/v3/api-docs`.

Para usar la interfaz React, inicia el repositorio del frontend por separado siguiendo sus propias instrucciones; por defecto debe ejecutarse en `http://localhost:5173`.

### Configuración local

La aplicación crea la base `sonido_vivo` si no existe y utiliza estos valores locales predeterminados:

| Variable | Valor local predeterminado |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/sonido_vivo?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago` |
| `DB_USERNAME` | `sonidovivo` |
| `DB_PASSWORD` | `sonidovivo` |
| `JWT_SECRET` | Clave de desarrollo incluida en la configuración local |

Para otros entornos, define `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` como variables de entorno. No uses las credenciales o la clave de desarrollo en producción.

### Alternativa: API y MySQL con Docker

Esta alternativa reemplaza el inicio local de Spring Boot. Primero genera el JAR y luego levanta ambos servicios:

```powershell
.\mvnw.cmd clean package
docker compose up --build
```

La API seguirá disponible en `http://localhost:8080`. El frontend puede mantenerse localmente y debe configurarse para consumir esa URL.

### Detener servicios

- Detén la API local con `Ctrl + C`.
- Detén los contenedores y conserva los datos con:

  ```powershell
  docker compose down
  ```

- Para eliminar también el volumen de MySQL y comenzar desde cero, usa `docker compose down -v`.

### Validación

```powershell
.\mvnw.cmd test
```

## Usuarios iniciales

- ADMIN: `admin@sonidovivo.cl` / `admin123`
- VENDEDOR: `vendedor@sonidovivo.cl` / `vendedor123`

## Ejemplos

Login (`POST /auth/login`):
```json
{"email":"admin@sonidovivo.cl","password":"admin123"}
```
Respuesta `200`:
```json
{"token":"eyJ...","usuario":{"id":1,"nombre":"Administración Sonido Vivo","email":"admin@sonidovivo.cl","rol":"ADMIN"}}
```

Registro (`POST /auth/register`, `201`):
```json
{"nombre":"Ana Música","email":"ana@correo.cl","password":"secreta1","telefono":"+56912345678","direccion":"Calle 1 #234","comuna":"Viña del Mar","region":"Valparaíso"}
```

Crear pedido (`POST /api/pedidos`, JWT CLIENTE, `201`):
```json
{"items":[{"productoId":1,"cantidad":2}],"entrega":{"nombre":"Ana Música","correo":"ana@correo.cl","direccion":"Calle 1 #234","comuna":"Viña del Mar"}}
```

Cambio de estado (`PUT /api/pedidos/{id}/estado`, JWT ADMIN/VENDEDOR):
```json
{"estado":"Preparacion"}
```

Productos: `GET /api/productos?busqueda=guitarra&categoria=Cuerdas&stock=disponible&orden=precio&pagina=0&tamanio=12`; `POST`, `PUT` y `DELETE /api/productos` requieren ADMIN. Usuarios (`GET`, `PUT`, `DELETE /api/usuarios`) requieren ADMIN. Contacto y reparación son públicos con `POST /api/contacto` y `POST /api/reparaciones`; `GET /api/mensajes` requiere ADMIN o VENDEDOR.

Todas las respuestas de error siguen `{ "timestamp", "status", "message", "errors" }`: validación (400), sesión inválida/ausente (401), rol insuficiente (403), recurso inexistente (404) y fallos inesperados (500).