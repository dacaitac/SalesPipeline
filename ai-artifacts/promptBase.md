Actúa como un desarrollador Java y arquitecto de software. Tu objetivo es construir una capa de orquestación automatizada para validar prospectos de ventas (leads) antes de moverlos a la etapa de "Prospect".

### 1. Modelo de Dominio
Todo lead contiene la siguiente información personal básica: número de identificación nacional, fecha de nacimiento, nombre, apellido y correo electrónico. Utiliza tipos de datos inmutables (ej. `Records` en Java) para garantizar la seguridad del estado a través de múltiples hilos de ejecución.

### 2. Lógica de Validación y Orquestación
Implementa el siguiente flujo de validación manejando la concurrencia, los tiempos de espera y los fallos de red de forma resiliente:

* **Paso 1 (Ejecución en Paralelo):** Las dos validaciones siguientes no tienen dependencias entre sí y deben ejecutarse en paralelo.
    * **Validación de Registro Nacional:** La persona debe existir en el registro externo y sus datos coincidir con la base local.
    * **Revisión de Antecedentes Judiciales:** La persona no debe tener registros en el sistema externo de archivos nacionales.
* **Paso 2 (Ejecución Secuencial):**
    * **Buró de Cumplimiento (OFAC/Sanctions):** Requiere la salida exitosa de las dos validaciones previas.
    * *Caché y Resiliencia:* Implementa un mecanismo persistente/durable para cachear las respuestas de este buró y optimizar las llamadas. Si el servicio simulado cae, demuestra resiliencia manejando la falla de manera elegante o disparando un flujo de revisión manual.
* **Paso 3 (Ejecución Secuencial Final):**
    * **Puntaje de Calificación:** Requiere la salida limpia y exitosa del paso anterior. Un sistema interno provee un puntaje aleatorio entre 0 y 100; el lead se convierte solo si el puntaje supera 60.

### 3. Restricciones Técnicas y de Infraestructura
* **Lenguaje:** Java (JVM). El código (clases, métodos, variables) debe estar en inglés.
* **Interfaz:** Una CLI simple es suficiente; no construyas una interfaz de usuario ni una solución cliente-servidor.
* **Infraestructura:** No uses bases de datos externas ni colas de mensajes.
* **Sistemas Externos:** Implementa estos sistemas como funciones que retornen éxito o fracaso. Es obligatorio simular latencia en estas peticiones mediante stubs u otra técnica.

### 4. Arquitectura, Calidad de Código y Concurrencia
* **Arquitectura:** Aplica estrictamente los principios SOLID y la Arquitectura Hexagonal (Puertos y Adaptadores). El dominio debe estar completamente aislado de los detalles de infraestructura (CLI, almacenamiento local para el caché, stubs de red).
* **Patrones de Diseño:** Emplea patrones estructurales y de comportamiento adecuados. Usa el patrón *Strategy* para estandarizar las reglas de validación, un *Orchestrator* para coordinar el flujo, y un *Decorator* o *Proxy* para inyectar el caché y el manejo de fallos (circuit breaker/fallback) en el Buró de Cumplimiento.
* **Gestión de Hilos:** Implementa concurrencia estructurada mediante `CompletableFuture`. Está prohibido usar el `ForkJoinPool.commonPool()`. Inyecta un `ExecutorService` configurado a medida (ej. *fixed thread pool*). Implementa *timeouts* explícitos en las llamadas externas para prevenir bloqueos indefinidos de la CLI.
* **Observabilidad:** Implementa logging estructurado (ej. SLF4J). Configura MDC (Mapped Diagnostic Context) para inyectar el identificador del lead en los logs, permitiendo trazar su ciclo de vida a través del procesamiento paralelo y secuencial.
* **Código Limpio y Pruebas:** El código debe ser altamente testable mediante la inyección de dependencias. Documenta las interfaces y métodos críticos con Javadoc.