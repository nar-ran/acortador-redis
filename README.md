# ⚡ Acortador de Enlaces Reactivo con Redis

Este es un proyecto de portafolio de alto nivel desarrollado con **Java 21** y **Quarkus 3.15.1**. Su objetivo es demostrar una implementación avanzada y representativa de **Redis** utilizando múltiples estructuras de datos nativas.

El sistema sigue los principios de **Clean Architecture** (Arquitectura Limpia) para desacoplar completamente las reglas del negocio de los frameworks y la base de datos Redis. Todo el código, comentarios y documentación están escritos en **español**.

---

## 🛠️ Stack Tecnológico

*   **Java JDK 21** - Aprovechando las últimas características del lenguaje.
*   **Quarkus 3.15.1** - Framework Java reactivo, ágil y de alto rendimiento.
*   **Quarkus Redis Client** - Conector oficial reactivo basado en Vert.x para comunicarse con Redis de manera no bloqueante.
*   **RESTEasy Reactive (Jackson)** - API REST no bloqueante rápida con soporte de Server-Sent Events (SSE).
*   **HTML5 / CSS3 / Vanilla JS** - Dashboard interactivo integrado con estética premium en modo oscuro, *glassmorphism* y micro-animaciones.

---

## 📐 Estructura de Clean Architecture

El proyecto divide sus componentes para asegurar que el núcleo de negocio sea independiente de Redis y Quarkus:

*   `dominio`: Contiene las entidades puras de negocio (`Enlace`, `Analitica`), excepciones de dominio y las interfaces de los puertos de salida (`EnlaceRepositorioPort`, `AnaliticasRepositorioPort`). No tiene dependencias de librerías ni frameworks.
*   `aplicacion`: Implementa la lógica de negocio y los casos de uso (`AcortarUrlUseCase`, `ObtenerAnaliticasUseCase`) a través de servicios en Java puro.
*   `infraestructura`: Contiene las implementaciones técnicas concretas.
    *   `adaptadores/redis`: Conecta los puertos de dominio con Redis utilizando el cliente oficial.
    *   `adaptadores/rest`: Expone los controladores REST, redirecciones y el canal SSE para el dashboard.
    *   `configuracion`: Cablea los beans de aplicación mediante CDI y expone el filtro interceptor de Rate Limiting.

---

## 💾 Implementación de Estructuras en Redis

Este proyecto destaca por no usar Redis únicamente como una memoria caché genérica, sino utilizando sus estructuras de datos óptimas para cada tarea:

1.  **Strings (Clave-Valor) con Expiración**:
    *   **Estructura**: Clave `enlace:{codigo}` -> Valor `urlOriginal`.
    *   **Uso**: Permite una redirección ultra rápida en tiempo $O(1)$. Soporta expiración dinámica mediante TTL (`EXPIRE`), haciendo que los enlaces temporales se autodestruyan.
2.  **Hashes**:
    *   **Estructura**: Clave `enlace:{codigo}:analiticas` -> Mapa de campos (`total`, `chrome`, `firefox`, `safari`, `edge`, `otros`).
    *   **Uso**: Registra y agrupa las métricas detalladas por navegador para cada enlace acortado. Los contadores se incrementan atómicamente con `HINCRBY`.
3.  **Sorted Sets (Conjuntos Ordenados - ZSets)**:
    *   **Estructura**: Clave `enlaces:ranking` -> Elementos `codigo` ordenados por su cantidad de clics.
    *   **Uso**: Cada clic incrementa el score del código usando `ZINCRBY`. El endpoint `/api/enlaces/ranking` recupera instantáneamente el "Top 5" de enlaces más visitados de forma eficiente.
4.  **Strings con TTL (Rate Limiter)**:
    *   **Estructura**: Clave `limite:ip:{ip}` -> Contador entero con TTL de 60 segundos.
    *   **Uso**: Protege la API de abusos. Si una IP realiza más de 5 peticiones de acortamiento en menos de un minuto, el filtro HTTP intercepta la solicitud y devuelve un código `429 Too Many Requests`.
5.  **Pub/Sub (Publicación/Suscripción)**:
    *   **Estructura**: Canal `enlaces:actividad`.
    *   **Uso**: Cada redirección publica un mensaje JSON en el canal de Redis. El backend consume este canal de forma reactiva y transmite los clics en tiempo real al dashboard a través de Server-Sent Events (SSE).

---

## 🚀 Cómo Ejecutar el Proyecto

### 1. Configurar Redis
Dado que no se requiere Docker, puedes conectar este proyecto de dos formas:

*   **Opción A (Recomendada - En la nube gratis)**:
    1. Regístrate gratis en [Upstash](https://upstash.com) o [Redis Labs](https://redis.com/try-free/).
    2. Crea una base de datos Redis de capa gratuita.
    3. Copia la URL de conexión (ej. `redis://default:mi_password@host.upstash.io:6379`).
    4. Abre `src/main/resources/application.properties` y configura la propiedad:
       ```properties
       quarkus.redis.hosts=redis://default:mi_password@host.upstash.io:6379
       ```
*   **Opción B (Local en Windows)**:
    1. Instala **Memurai** (Redis nativo para Windows) o inicia Redis mediante **WSL2** (`sudo service redis-server start`).
    2. Mantén la configuración por defecto en `application.properties` (`redis://localhost:6379`).

### 2. Iniciar la Aplicación
1. Abre tu terminal de PowerShell en el directorio raíz del proyecto:
   ```powershell
   cd acortador-redis
   ```
2. Ejecuta la aplicación en modo desarrollo (Live Coding):
   ```powershell
   mvn quarkus:dev
   ```
3. Abre en tu navegador la URL: 👉 **[http://localhost:8080](http://localhost:8080)**

---

## 🖥️ Dashboard Web Premium

La aplicación sirve un panel de control interactivo en la raíz del servidor. Este dashboard cuenta con:
*   **Acortador en tiempo real**: Formulario animado para crear enlaces cortos con TTL.
*   **Simulador de clics**: Botón para probar la redirección y ver la velocidad de respuesta.
*   **Analíticas interactivas**: Gráficos de barras que muestran la distribución por navegador del enlace consultado.
*   **Top 5 dinámico**: Ranking en tiempo real ordenado automáticamente.
*   **Feed en vivo**: Consola conectada a SSE que muestra cada clic que ocurre en el sistema en tiempo real.
*   **Prueba de Rate Limiting**: Si intentas crear más de 5 enlaces en 60 segundos, verás aparecer una notificación flotante de advertencia animada.
