# Registro de ingeniería con IA (Junie)

## 1. Rol del alumno y de Junie

- Junie: implementó las fases solicitadas del plan, mantuvo los cambios separados por fase, ejecutó las pruebas indicadas y documentó las decisiones técnicas conocidas.
- Alumno: elegí el tema (gestor de inventario de un restaurante), preparé el plan por fases que se le dio a Junie (`PLAN_JUNIE.md`, elaborado con ayuda de otra IA, Claude, a partir del enunciado), lancé cada fase por separado, revisé el resultado y los commits, y probé la aplicación en el navegador. La parte de Docker (Dockerfile, docker-compose, `.env`) la hice yo a mano siguiendo una guía paso a paso, y completé la sección de despliegue del README con capturas de la prueba de persistencia.

## 2. Prompts clave por fase (prompt literal + resumen del resultado)

- PLAN — Antes de usar Junie, generé con Claude un plan de 7 fases (`PLAN_JUNIE.md`) a partir del enunciado de la práctica: stack, arquitectura por capas, reglas obligatorias (DTOs, `PreparedStatement`, variables de entorno, códigos HTTP), tests por fase y commits atómicos. El plan prohibía expresamente que Junie creara archivos de Docker.
- Los prompts a Junie fueron los indicados arriba: el primero indicaba leer el plan completo y ejecutar solo la fase 1; los siguientes pedían continuar con cada fase.

- FASE 1 — Prompt: «Lee PLAN_JUNIE.md entero, incluyendo las reglas de la sección 0. Ejecuta únicamente la FASE 1 y para. Cuando termines, ejecuta mvn -q test, haz el commit indicado en el plan y dime qué archivos has creado». Resultado: estructura Maven WAR, esquema inicial MySQL/H2 y documentación base; pruebas ejecutadas y commit de fase creado.
- FASE 2 — Prompt: «continua con la fase 2». Resultado: configuración, modelo, excepciones, DTOs y mappers según el plan.
- FASE 3 — Prompt: «Sigue con la FASE 3». Resultado: DAOs JDBC y pruebas de integración con H2; se crearon los commits de implementación y pruebas (`edf426e`, `dd02fcc`).
- FASE 4 — Prompt: «continúa con la fase 4». Resultado: servicios, validaciones, transacciones y pruebas; commits `985418f` y `fde1a74`.
- FASE 5 — Prompt: «continúa con la fase 5». Resultado: API REST con servlets, JSON, códigos HTTP y pruebas de contrato; commit `52ace01`.
- FASE 6 — Prompt: «continúa con la fase 6». Resultado: interfaz web responsive, cliente `fetch`, gestión de formularios, filtros y errores; commit `ef2cad3`.
- FASE 7 — Prompt: «sigue con la fase 7». Resultado: documentación del modelo, arquitectura, endpoints, variables de entorno y pruebas; el apartado de despliegue con Docker lo completó después el alumno (ver sección 2 bis).
## 2 bis. Parte de Docker (hecha por el alumno, sin Junie)

Por decisión propia, Junie no creó ningún archivo de Docker (el plan se lo prohibía expresamente). El alumno los escribió a mano siguiendo una guía paso a paso elaborada con un asistente de IA (Claude), y los revisó y probó él mismo:

- `.env.example` y `.env`: variables de conexión y puertos; el `.env` real queda fuera de Git.
- `.dockerignore`: excluye `target/`, `.git/`, `.idea/` y `.env` del contexto de construcción.
- `Dockerfile` multi-stage: compila `ROOT.war` con Maven y lo despliega en Tomcat 10.1.
- `docker-compose.yml`: servicios `db` (MySQL con healthcheck y volumen `db-data`), `app` y `adminer`, red interna `inventario-net` y `depends_on` con `service_healthy`.
- Comprobación: `docker compose up --build -d` levanta los tres contenedores, la aplicación funciona en `localhost:8080` y los datos se ven en Adminer.
- Incidencia resuelta: `docker compose config` falló con un error de esquema en `services.networks.inventario-net` porque, al pegar el archivo, los bloques `volumes:` y `networks:` quedaron con sangría dentro de `services:`. Se corrigió dejándolos en la primera columna.

## 3. Decisiones arquitectónicas (y por qué)

- Se conserva la separación Controlador → Servicio → DAO para aislar HTTP, reglas de negocio y persistencia.
- Los servlets intercambian DTOs; los mappers mantienen la conversión entre DTOs y modelos fuera de la API.
- JDBC parametrizado y HikariCP proporcionan acceso a MySQL; las variables de entorno evitan incluir credenciales en el código.
- El registro de movimientos actualiza el stock dentro de una transacción para mantener coherentes el histórico y las existencias.
- H2 permite probar las consultas de los DAOs sin una instancia MySQL, y Mockito permite aislar los servicios.
- La interfaz usa HTML/CSS/JavaScript nativo y renderiza los datos con APIs de texto del DOM para evitar interpretar contenido del usuario como HTML.
- Tema: inventario de restaurante, con productos, categorías, proveedores y movimientos de stock, porque permite una relación 1:N (categoría–producto), una N:M con atributo (producto–proveedor) y una regla de negocio transaccional (movimientos).
- El stock no se edita a mano: solo cambia registrando movimientos, que no se pueden editar ni borrar, para conservar un histórico coherente.
- Despliegue en Tomcat 10.1 con WAR `ROOT.war`, para que la aplicación quede en la raíz sin prefijo.
- Docker lo hice yo a mano en lugar de delegarlo en Junie, para entender la orquestación: Dockerfile multi-stage, healthcheck en MySQL con `depends_on: service_healthy`, red interna, volumen nombrado `db-data` para la persistencia y Adminer para auditar los datos.
- La base de datos no expone ningún puerto al exterior; solo la aplicación y Adminer llegan a ella por la red interna.

## 4. Errores de Junie corregidos manualmente

- Revisé el resultado de cada fase y probé la aplicación en el navegador (nombres duplicados, borrado de una categoría con productos, salidas mayores que el stock y formularios vacíos) sin encontrar errores que corregir a mano en el código de Junie.
## 5. Refactorizaciones hechas por el alumno

- No realicé refactorizaciones en el código Java ni en la interfaz de Junie; mi trabajo se centró en la revisión, las pruebas, la configuración de Docker y la documentación.
## 6. Qué aprendí / limitaciones de la IA

- Aprendí cómo se organiza una aplicación por capas (Controlador → Servicio → DAO → BD), por qué se usan DTOs y cómo una transacción mantiene coherentes el histórico de movimientos y el stock. Con Docker aprendí qué hacen un healthcheck y `depends_on`, para qué sirve un volumen nombrado y por qué `init.sql` solo se ejecuta con el volumen vacío.
- Límites de la IA que observé: no ve mi entorno, así que dependí de ejecutar los comandos yo mismo y de pegarle los errores; un archivo YAML generado puede romperse al pegarlo por la indentación (me pasó con `networks:`); y la documentación escrita por una IA hay que contrastarla con la aplicación real antes de entregarla.
- La próxima vez haría desde el principio pruebas manuales con casos límite y anotaría los errores a medida que aparezcan, en lugar de reconstruirlos al final.