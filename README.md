# Sonido Vivo Backend

API REST con Spring Boot 3, Java 21, MySQL 8, JWT, BCrypt y Swagger.

## Ejecución integrada con el frontend

El procedimiento completo está documentado en `A:\Escritorio\proyecto sonido vivo\GUIA_EJECUCION.md`. La opción recomendada ejecuta MySQL con Docker y deja backend y frontend en local.

**Requisitos:** Docker Desktop iniciado, Java 21 o superior, Node.js y los puertos `3306`, `8080` y `5173` disponibles.

Abre tres terminales de PowerShell:

```powershell
# Terminal 1 — MySQL
cd "A:\Escritorio\proyecto sonido vivo\Proyecto_sonido_vivo_backend"
docker compose up -d mysql
docker compose ps

# Terminal 2 — Backend
cd "A:\Escritorio\proyecto sonido vivo\Proyecto_sonido_vivo_backend"
.\mvnw.cmd spring-boot:run

# Terminal 3 — Frontend
cd "A:\Escritorio\proyecto sonido vivo\Proyecto_Sonido_vivo_frontend"
npm install
npm run dev
```

Espera a que MySQL esté en estado `healthy` o `running`. Después abre el frontend en `http://localhost:5173`, Swagger en `http://localhost:8080/swagger-ui.html` u OpenAPI en `http://localhost:8080/v3/api-docs`.

La conexión local usa `sonidovivo` / `sonidovivo` y crea la base `sonido_vivo` si es necesario. Configura `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` para otros entornos.

### Alternativa: backend y MySQL con Docker

Usa esta opción en lugar de iniciar el backend con Maven; el frontend se mantiene local con `npm run dev`.

```powershell
cd "A:\Escritorio\proyecto sonido vivo\Proyecto_sonido_vivo_backend"
.\mvnw.cmd clean package
docker compose up --build
```

Para detener backend y frontend usa `Ctrl + C`. Para apagar MySQL conservando los datos, ejecuta `docker compose down`; utiliza `docker compose down -v` solo si también quieres eliminar los datos de la base.

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