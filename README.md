# Sonido Vivo Backend

API REST con Spring Boot 3, Java 21, MySQL 8, JWT, BCrypt y Swagger.

## Levantar localmente

1. Inicia MySQL con `docker compose up -d mysql`.
2. Compila y ejecuta: `./mvnw.cmd clean package` y `./mvnw.cmd spring-boot:run`.
3. Abre Swagger en `http://localhost:8080/swagger-ui.html`; OpenAPI está en `http://localhost:8080/v3/api-docs`.

La conexión local usa `sonidovivo` / `sonidovivo` y crea la base `sonido_vivo` si es necesario. Configura `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` para otros entornos. Para Docker completo, primero ejecuta `./mvnw.cmd clean package` y luego `docker compose up --build`.

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
{"items":[{"productoId":1,"cantidad":2}]}
```

Cambio de estado (`PUT /api/pedidos/{id}/estado`, JWT ADMIN/VENDEDOR):
```json
{"estado":"Preparacion"}
```

Productos: `GET /api/productos?busqueda=guitarra&categoria=Cuerdas&stock=disponible&orden=precio&pagina=0&tamanio=12`; `POST`, `PUT` y `DELETE /api/productos` requieren ADMIN. Usuarios (`GET`, `PUT`, `DELETE /api/usuarios`) requieren ADMIN. Contacto y reparación son públicos con `POST /api/contacto` y `POST /api/reparaciones`; `GET /api/mensajes` requiere ADMIN o VENDEDOR.

Todas las respuestas de error siguen `{ "timestamp", "status", "message", "errors" }`: validación (400), sesión inválida/ausente (401), rol insuficiente (403), recurso inexistente (404) y fallos inesperados (500).