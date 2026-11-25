# Plan para Junie — Gestor de Inventario de Restaurante

> **Cómo usar este archivo:** guárdalo en la raíz del proyecto como `PLAN_JUNIE.md`.
> Después, en Junie escribe:
> *"Lee PLAN_JUNIE.md entero. Ejecuta la FASE 1 y para. Cuando termines, dime qué archivos has creado y ejecuta `mvn test`."*
> Repite con la FASE 2, 3… Así tienes commits atómicos (lo pide la rúbrica) y puedes revisar cada fase.
> Si lo quieres "de una", dile: *"Ejecuta todas las fases en orden, una detrás de otra, y haz `git commit` al final de cada fase con el mensaje indicado."*

---

## 0. Contexto y reglas (léelas siempre antes de cada fase)

Eres el desarrollador de una aplicación web Java para gestionar el **inventario de un restaurante**: productos (ingredientes, bebidas, material), sus categorías, sus proveedores y los movimientos de stock (entradas, salidas y mermas), con alertas de stock bajo.

Es una práctica de **Acceso a Datos (2.º DAM)**. La corrección se basa en una rúbrica, así que estas reglas son **obligatorias**:

1. **Stack fijo:**
   - Java 21, Maven, empaquetado **WAR** con `<finalName>ROOT</finalName>` (para que la app quede en `/`).
   - **Jakarta Servlet 6.0** (`jakarta.servlet-api`, scope `provided`) → se desplegará en **Tomcat 10.1**.
   - **JDBC puro** con `PreparedStatement` + pool **HikariCP**. Driver `mysql-connector-j`.
   - **Jackson** (`jackson-databind` + `jackson-datatype-jsr310`) para JSON.
   - Tests: **JUnit 5**, **Mockito** (`mockito-junit-jupiter`) y **H2** (scope `test`) en modo MySQL.
   - Frontend: **HTML5 semántico + CSS3 + JavaScript moderno con `fetch`**. Sin frameworks, sin JSP, sin Java ni SQL en las vistas.
2. **Arquitectura por capas estricta:** `Controlador (Servlet) → Servicio → DAO → BD`. Ningún servlet toca SQL ni DAOs. Ningún DAO tiene lógica de negocio. Sin dependencias circulares.
3. **DTOs obligatorios:** los servlets reciben y devuelven **solo DTOs** (Java `record`). Las entidades del paquete `model` nunca salen de la capa de servicio. Conversión en clases `mapper`.
4. **Nunca** concatenar valores en SQL. Siempre `PreparedStatement` con `?`. Siempre **try-with-resources** para `Connection`, `PreparedStatement` y `ResultSet`.
5. **Prohibido hardcodear credenciales o URLs.** La conexión se lee de variables de entorno: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (con `System.getenv`). Si falta alguna obligatoria, lanzar una excepción clara al arrancar.
6. **Excepciones personalizadas** y mapeo a códigos HTTP semánticos:
   | Excepción | HTTP |
   |---|---|
   | `ValidacionException` | 400 |
   | `RecursoNoEncontradoException` | 404 |
   | `ConflictoException` (clave duplicada, borrar algo con dependencias, stock insuficiente) | 409 |
   | `DataAccessException` (envuelve `SQLException`) | 500 |
   Creación correcta → **201** con el recurso creado. Borrado correcto → **204**. Resto → **200**.
   Todas las respuestas de error en JSON: `{"status": 400, "error": "Bad Request", "mensaje": "...", "detalles": {"campo": "motivo"}}`.
7. **No crees** `Dockerfile`, `docker-compose.yml`, `.env` ni `.env.example`: esa parte la hace el alumno a mano. Sí creas `db/init.sql`.
8. Código, nombres de clases, comentarios y textos de la interfaz **en español**.
9. Al terminar cada fase, ejecuta `mvn -q test` y no des la fase por cerrada si algo falla.

### Estructura de paquetes

```
src/main/java/es/restaurante/inventario/
├── config/       DataSourceProvider, AppContextListener, JsonMapper
├── model/        Categoria, Proveedor, Producto, ProductoProveedor, MovimientoStock, TipoMovimiento (enum), UnidadMedida (enum)
├── dto/          *Dto (records de salida) y *RequestDto (records de entrada)
├── mapper/       conversión model <-> dto
├── dao/          interfaces (CategoriaDao, ...) 
├── dao/jdbc/     implementaciones JDBC (CategoriaDaoJdbc, ...)
├── service/      CategoriaService, ProveedorService, ProductoService, MovimientoService, DashboardService
├── controller/   BaseApiServlet + un servlet por recurso
└── exception/    excepciones personalizadas
src/main/webapp/
├── index.html, productos.html, categorias.html, proveedores.html, movimientos.html
├── css/styles.css
└── js/api.js, js/ui.js, js/<pagina>.js
src/test/java/...  (mismos paquetes)
src/test/resources/schema-h2.sql
db/init.sql
```

---

## FASE 1 — Proyecto Maven y base de datos

**Objetivo:** esqueleto del proyecto, `pom.xml` y modelo relacional.

1. Crea `pom.xml` con el stack de la sección 0 (versiones estables actuales), `maven-war-plugin` y `maven-surefire-plugin` reciente para JUnit 5. `failOnMissingWebXml=false`.
2. Crea `.gitignore` (target/, .idea/, *.iml, .env, *.log, out/).
3. Crea `db/init.sql` (MySQL 8) con `SET NAMES utf8mb4;` al principio, tablas en `utf8mb4` y motor InnoDB:

   - **categoria**: `id` PK autoincremental, `nombre` VARCHAR(60) NOT NULL **UNIQUE**, `descripcion` VARCHAR(255).
   - **proveedor**: `id` PK, `nombre` VARCHAR(100) NOT NULL, `cif` VARCHAR(9) NOT NULL **UNIQUE**, `telefono` VARCHAR(20), `email` VARCHAR(120) UNIQUE.
   - **producto**: `id` PK, `nombre` VARCHAR(100) NOT NULL **UNIQUE**, `categoria_id` FK → categoria (**ON DELETE RESTRICT ON UPDATE CASCADE**) → relación **1:N**, `unidad_medida` VARCHAR(10) NOT NULL con `CHECK (unidad_medida IN ('KG','L','UD'))`, `stock_actual` DECIMAL(10,2) NOT NULL DEFAULT 0 con `CHECK (stock_actual >= 0)`, `stock_minimo` DECIMAL(10,2) NOT NULL DEFAULT 0, `precio_unitario` DECIMAL(10,2) NOT NULL, `fecha_actualizacion` DATETIME NOT NULL (la rellena el DAO, sin `ON UPDATE`).
   - **producto_proveedor** (relación **N:M** con atributo): PK compuesta (`producto_id`, `proveedor_id`), ambas FK con **ON DELETE CASCADE**, `precio_compra` DECIMAL(10,2) NOT NULL.
   - **movimiento_stock**: `id` PK, `producto_id` FK → producto (**ON DELETE CASCADE**), `tipo` VARCHAR(10) NOT NULL con `CHECK (tipo IN ('ENTRADA','SALIDA','MERMA'))`, `cantidad` DECIMAL(10,2) NOT NULL `CHECK (cantidad > 0)`, `fecha` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, `nota` VARCHAR(255).
   - Índices en las FK.
   - **Datos de prueba (DML)** realistas de restaurante: 5 categorías (Carnes, Pescados, Verduras y frutas, Bebidas, Limpieza), 4 proveedores con CIF válido de formato, ~15 productos (algunos **por debajo del stock mínimo** para que salgan alertas), relaciones producto–proveedor y ~20 movimientos.
   - No uses `CREATE DATABASE` ni `USE`: la base de datos la crea el contenedor de MySQL.
4. Crea `src/test/resources/schema-h2.sql` con **la misma estructura** pero compatible con H2 en modo MySQL (sin `ENGINE`, sin `SET NAMES`), sin datos.
5. Crea `README.md` vacío con solo el título y `AI_WORKFLOW.md` con la plantilla de la FASE 7.

**Commit:** `chore: estructura Maven y script init.sql con modelo relacional`

---

## FASE 2 — Configuración, modelo, excepciones y DTOs

1. `config/DataSourceProvider`: construye un `HikariDataSource` leyendo **solo** las variables de entorno de la regla 5. URL: `jdbc:mysql://{host}:{port}/{name}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Madrid&characterEncoding=UTF-8`. Pool máx. 10. `driverClassName` = `com.mysql.cj.jdbc.Driver`. Debe permitir inyectar un `DataSource` externo (para tests).
2. `config/AppContextListener` (`@WebListener`): crea el DataSource al arrancar, instancia DAOs y servicios (inyección manual por constructor) y los guarda en el `ServletContext`; cierra el pool en `contextDestroyed`. **Reintentos**: si la BD no responde, reintentar 5 veces con 3 s de espera antes de fallar.
3. `config/JsonMapper`: `ObjectMapper` único con `JavaTimeModule` y fechas ISO (no timestamps).
4. Clases `model` (POJOs) y enums `TipoMovimiento`, `UnidadMedida`.
5. Excepciones de la regla 6 (todas `RuntimeException`). `ValidacionException` lleva un `Map<String,String>` de errores por campo.
6. DTOs como `record`:
   - Entrada: `CategoriaRequestDto`, `ProveedorRequestDto`, `ProductoRequestDto` (nombre, categoriaId, unidadMedida, stockMinimo, precioUnitario — **el stock no se edita a mano**, solo con movimientos), `MovimientoRequestDto` (productoId, tipo, cantidad, nota), `ProductoProveedorRequestDto` (proveedorId, precioCompra).
   - Salida: `CategoriaDto`, `ProveedorDto`, `ProductoDto` (incluye `categoriaNombre` y `bajoStock` boolean), `MovimientoDto` (incluye `productoNombre`), `ProductoProveedorDto` (incluye nombre del proveedor), `DashboardDto` (totalProductos, totalCategorias, totalProveedores, valorInventario, productosBajoStock: lista).
7. Mappers en `mapper/`.

**Commit:** `feat: configuración por variables de entorno, modelo, DTOs y excepciones`

---

## FASE 3 — Capa DAO (JDBC)

1. Interfaces en `dao/` e implementaciones en `dao/jdbc/`. Cada implementación recibe un `DataSource` por constructor.
2. Operaciones:
   - `CategoriaDao`: `findAll`, `findById` (Optional), `insert` (devuelve id generado con `RETURN_GENERATED_KEYS`), `update`, `delete`, `existsByNombre`, `countProductos(categoriaId)`.
   - `ProveedorDao`: CRUD + `existsByCif`.
   - `ProductoDao`: CRUD, `findAll(Long categoriaId, boolean soloBajoStock)` con JOIN a categoría (filtros opcionales **con parámetros**, nunca concatenando valores), `findByIdForUpdate(Connection, id)` (`SELECT ... FOR UPDATE`), `updateStock(Connection, id, nuevoStock)`.
   - `ProductoProveedorDao`: `findByProducto`, `insert`, `delete`.
   - `MovimientoDao`: `findAll(Long productoId)` ordenado por fecha desc, `insert(Connection, movimiento)`.
3. Los métodos que reciben `Connection` **no la cierran** (la gestiona el servicio para la transacción).
4. Traducción de errores: `SQLIntegrityConstraintViolationException` (o SQLState que empiece por `23`) → `ConflictoException` con mensaje legible ("Ya existe una categoría con ese nombre"); cualquier otra `SQLException` → `DataAccessException`.

**Tests (FASE 3):** `src/test/java/.../dao/jdbc/*DaoJdbcTest` con H2 en memoria (`jdbc:h2:mem:test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1`), ejecutando `schema-h2.sql` y limpiando tablas en `@BeforeEach`. Para cada DAO:
- happy path de **insert, findById, findAll, update, delete**;
- `findById` de un id inexistente → `Optional.empty()`;
- **clave duplicada** (nombre de categoría, CIF de proveedor) → `ConflictoException`;
- borrar una categoría con productos → `ConflictoException` (FK RESTRICT);
- borrar un producto borra en cascada sus movimientos y relaciones con proveedores;
- filtro `soloBajoStock` devuelve solo los que cumplen `stock_actual < stock_minimo`.

**Commit:** `feat: capa DAO con JDBC, PreparedStatement y pool HikariCP` y `test: pruebas de integración de los DAO con H2`

---

## FASE 4 — Capa de servicio (lógica de negocio)

1. Servicios que reciben DAOs (y el `DataSource` donde haga falta transacción) por constructor. Reciben y devuelven **DTOs**.
2. Validaciones en servidor (lanzan `ValidacionException` con todos los errores por campo a la vez):
   - Nombres obligatorios, sin espacios sobrantes (`trim`), longitudes máximas como en la BD.
   - CIF con formato `^[A-Z]\d{7}[A-Z0-9]$`; email con formato válido si viene; teléfono solo dígitos, espacios y `+`.
   - Precios y stock mínimo ≥ 0; cantidad de movimiento > 0; tipo y unidad válidos.
   - `categoriaId` / `productoId` / `proveedorId` existentes → si no, `RecursoNoEncontradoException`.
3. Reglas de negocio:
   - No se puede borrar una categoría con productos → `ConflictoException` ("La categoría tiene N productos").
   - **`MovimientoService.registrar`** en **una transacción**: obtener conexión, `setAutoCommit(false)`, bloquear el producto con `findByIdForUpdate`, calcular stock nuevo (ENTRADA suma; SALIDA y MERMA restan), si queda negativo → `ConflictoException("Stock insuficiente: disponible X kg")`, insertar movimiento, actualizar stock y `fecha_actualizacion`, `commit`. Ante cualquier error, `rollback` y relanzar. Restaurar autocommit y cerrar en `finally`/try-with-resources.
   - `ProductoDto.bajoStock` = `stockActual < stockMinimo`.
   - `DashboardService`: totales, valor del inventario (Σ stock × precio) y lista de productos bajo stock.

**Tests (FASE 4):** `*ServiceTest` con **Mockito** (`@ExtendWith(MockitoExtension.class)`), DAOs mockeados:
- happy path de crear/actualizar/borrar/listar;
- validación con datos inválidos → `ValidacionException` y comprobar los campos del mapa;
- id inexistente → `RecursoNoEncontradoException`;
- borrar categoría con productos → `ConflictoException` y `verify(dao, never()).delete(...)`;
- movimiento SALIDA mayor que el stock → `ConflictoException` y **rollback** verificado (`verify(connection).rollback()`);
- movimiento ENTRADA correcto → `commit` y stock actualizado.
- Además, un **test de integración** de `MovimientoService` con H2 real que compruebe que tras un error el stock no cambia.

**Commit:** `feat: capa de servicio con validaciones y transacciones` y `test: pruebas unitarias de servicios con Mockito`

---

## FASE 5 — Capa de control (API REST con Servlets)

1. `BaseApiServlet` (abstracta, extiende `HttpServlet`): lee el JSON del body a un DTO, escribe JSON con `Content-Type: application/json; charset=UTF-8`, parsea el id de `pathInfo`, y **captura las excepciones** de la regla 6 convirtiéndolas al JSON de error con su código. JSON mal formado → 400. Id no numérico → 400. Excepción no prevista → 500 con mensaje genérico (sin stacktrace al cliente; sí al log).
2. Servlets (solo llaman al servicio; nada de lógica):

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/categorias` | 200 lista |
| GET | `/api/categorias/{id}` | 200 / 404 |
| POST | `/api/categorias` | 201 / 400 / 409 |
| PUT | `/api/categorias/{id}` | 200 / 400 / 404 / 409 |
| DELETE | `/api/categorias/{id}` | 204 / 404 / 409 |
| (igual) | `/api/proveedores[/{id}]` | |
| (igual) | `/api/productos[/{id}]` — GET admite `?categoriaId=&bajoStock=true` | |
| GET | `/api/productos/{id}/proveedores` | 200 |
| POST | `/api/productos/{id}/proveedores` | 201 / 404 / 409 |
| DELETE | `/api/productos/{id}/proveedores/{proveedorId}` | 204 / 404 |
| GET | `/api/movimientos?productoId=` | 200 |
| POST | `/api/movimientos` | 201 / 400 / 404 / 409 |
| GET | `/api/dashboard` | 200 |

   Los movimientos no se editan ni borran (son histórico): explícalo en el README.
3. Mapeo con `@WebServlet("/api/categorias/*")` etc. Los servlets obtienen los servicios del `ServletContext`.

**Commit:** `feat: API REST con servlets, JSON y códigos HTTP semánticos`

---

## FASE 6 — Interfaz web

1. Páginas: `index.html` (panel: tarjetas con totales, valor del inventario y **tabla de alertas de stock bajo**), `productos.html`, `categorias.html`, `proveedores.html`, `movimientos.html`. Menú de navegación común en `<header><nav>`, contenido en `<main>`, HTML semántico y accesible (`label` en cada input, `aria-live` para mensajes).
2. `js/api.js`: wrapper de `fetch` (async/await) que lanza un error con el `mensaje` y `detalles` del JSON de error; `js/ui.js`: toasts de éxito/error, spinner/estado de carga, diálogo de confirmación con `<dialog>`, render de errores por campo debajo de cada input.
3. Cada página: tabla con listado, formulario (crear/editar en un `<dialog>`), botones editar/borrar con confirmación, **estado de carga** mientras se espera, **mensajes de error claros** (validación 400 junto al campo; 409 y 500 en toast), estado vacío ("No hay productos todavía").
   - Productos: filtro por categoría y casilla "solo bajo stock"; filas bajo stock resaltadas; botón "Proveedores" que abre un diálogo para ver/añadir/quitar proveedores con su precio de compra.
   - Movimientos: formulario de producto (select) + tipo + cantidad + nota, e histórico filtrable por producto. Tras registrar, refrescar.
4. `css/styles.css`: diseño limpio con variables CSS, **responsive** (en móvil las tablas pasan a tarjetas o hacen scroll horizontal), modo claro, colores coherentes (verde ok, ámbar bajo stock, rojo error).
5. Requisito: **cero errores en la consola del navegador**. Escapa siempre el texto que pintes (usa `textContent`, no `innerHTML` con datos del usuario).

**Commit:** `feat: interfaz web responsiva con fetch, estados de carga y errores`

---

## FASE 7 — Documentación

1. **README.md** con: descripción, stack, **diagrama E-R en Mermaid** (`erDiagram`) con las 5 tablas y cardinalidades, **esquema de arquitectura** de capas (Mermaid `flowchart`), tabla de endpoints, explicación de cada variable de entorno (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_ROOT_PASSWORD`), cómo ejecutar los tests (`mvn test`), y **deja una sección `## Despliegue con Docker` con un `TODO`** que completará el alumno (también el diagrama de contenedores).
2. **AI_WORKFLOW.md** con esta plantilla, rellenando lo que hayas hecho tú y dejando huecos marcados `TODO (alumno)` para lo que debe escribir el alumno:

```markdown
# Registro de ingeniería con IA (Junie)
## 1. Rol del alumno y de Junie
## 2. Prompts clave por fase (prompt literal + resumen del resultado)
## 3. Decisiones arquitectónicas (y por qué)
## 4. Errores de Junie corregidos manualmente
## 5. Refactorizaciones hechas por el alumno
## 6. Qué aprendí / limitaciones de la IA
```

**Commit:** `docs: README con diagrama E-R, arquitectura y endpoints; AI_WORKFLOW`

---

## Checklist final para Junie

- [ ] `mvn clean package` genera `target/ROOT.war` sin errores.
- [ ] `mvn test` pasa entero (DAO con H2 + servicios con Mockito).
- [ ] `grep -ri "password\|jdbc:mysql" src/main/java` no encuentra credenciales escritas (solo la plantilla de URL con variables).
- [ ] Ningún `Statement` concatenado; todo `PreparedStatement`.
- [ ] Ningún servlet importa nada de `dao` ni `model`.
- [ ] Ninguna entidad `model` se serializa a JSON.
