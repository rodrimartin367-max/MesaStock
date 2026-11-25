# Registro de ingeniería con IA (Junie)

## 1. Rol del alumno y de Junie

- Junie: implementó las fases solicitadas del plan, mantuvo los cambios separados por fase, ejecutó las pruebas indicadas y documentó las decisiones técnicas conocidas.
- Alumno: revisar el código y los commits, validar el comportamiento en su entorno y completar las tareas que se señalan como `TODO (alumno)`.
- TODO (alumno): describir cómo se repartió el trabajo y qué decisiones o cambios realizó personalmente.

## 2. Prompts clave por fase (prompt literal + resumen del resultado)

- FASE 1 — Prompt: «Lee PLAN_JUNIE.md entero, incluyendo las reglas de la sección 0. Ejecuta únicamente la FASE 1 y para. Cuando termines, ejecuta mvn -q test, haz el commit indicado en el plan y dime qué archivos has creado». Resultado: estructura Maven WAR, esquema inicial MySQL/H2 y documentación base; pruebas ejecutadas y commit de fase creado.
- FASE 2 — Prompt: «continua con la fase 2». Resultado: configuración, modelo, excepciones, DTOs y mappers según el plan.
- FASE 3 — Prompt: «Sigue con la FASE 3». Resultado: DAOs JDBC y pruebas de integración con H2; se crearon los commits de implementación y pruebas (`edf426e`, `dd02fcc`).
- FASE 4 — Prompt: «continúa con la fase 4». Resultado: servicios, validaciones, transacciones y pruebas; commits `985418f` y `fde1a74`.
- FASE 5 — Prompt: «continúa con la fase 5». Resultado: API REST con servlets, JSON, códigos HTTP y pruebas de contrato; commit `52ace01`.
- FASE 6 — Prompt: «continúa con la fase 6». Resultado: interfaz web responsive, cliente `fetch`, gestión de formularios, filtros y errores; commit `ef2cad3`.
- FASE 7 — Prompt: «sigue con la fase 7». Resultado: documentación del modelo, arquitectura, endpoints, variables de entorno y pruebas; el despliegue Docker permanece pendiente del alumno.
- TODO (alumno): añadir o corregir los prompts si el historial de la conversación contiene instrucciones relevantes que no estén recogidas aquí.

## 3. Decisiones arquitectónicas (y por qué)

- Se conserva la separación Controlador → Servicio → DAO para aislar HTTP, reglas de negocio y persistencia.
- Los servlets intercambian DTOs; los mappers mantienen la conversión entre DTOs y modelos fuera de la API.
- JDBC parametrizado y HikariCP proporcionan acceso a MySQL; las variables de entorno evitan incluir credenciales en el código.
- El registro de movimientos actualiza el stock dentro de una transacción para mantener coherentes el histórico y las existencias.
- H2 permite probar las consultas de los DAOs sin una instancia MySQL, y Mockito permite aislar los servicios.
- La interfaz usa HTML/CSS/JavaScript nativo y renderiza los datos con APIs de texto del DOM para evitar interpretar contenido del usuario como HTML.
- TODO (alumno): documentar decisiones propias o alternativas valoradas durante la práctica.

## 4. Errores de Junie corregidos manualmente

- TODO (alumno): anotar cada error detectado, el archivo afectado y cómo se corrigió. No se registran aquí correcciones manuales no confirmadas.

## 5. Refactorizaciones hechas por el alumno

- TODO (alumno): describir las refactorizaciones realizadas personalmente o indicar que no se hicieron.

## 6. Qué aprendí / limitaciones de la IA

- TODO (alumno): completar con aprendizajes propios, limitaciones observadas y ejemplos revisados durante la práctica.