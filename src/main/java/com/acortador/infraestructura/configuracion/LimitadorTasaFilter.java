package com.acortador.infraestructura.configuracion;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

// Filtro HTTP que intercepta la creación de enlaces y aplica un Rate Limit de 5 peticiones por minuto.
public class LimitadorTasaFilter {

    private final ValueCommands<String, Long> comandosValor;
    private final KeyCommands<String> comandosClave;

    @Inject
    public LimitadorTasaFilter(RedisDataSource ds) {
        this.comandosValor = ds.value(Long.class);
        this.comandosClave = ds.key();
    }

    @ServerRequestFilter
    public Response interceptarPeticion(ContainerRequestContext contexto) {
        String ruta = contexto.getUriInfo().getPath();
        String metodo = contexto.getMethod();

        // Aplicamos el límite únicamente para la creación de enlaces cortos (POST /api/enlaces)
        if ("api/enlaces".equals(ruta) || "/api/enlaces".equals(ruta)) {
            if ("POST".equalsIgnoreCase(metodo)) {
                String ipCliente = obtenerIpCliente(contexto);
                String claveLimite = "limite:ip:" + ipCliente;

                // Incrementa de forma atómica el número de peticiones de esa IP
                Long peticiones = comandosValor.incr(claveLimite);

                // Si es la primera petición en la ventana de tiempo, configuramos el TTL (60 segundos)
                if (peticiones == 1) {
                    comandosClave.expire(claveLimite, 60);
                }

                // Si supera las 5 peticiones por minuto, rechazamos con HTTP 429 (Too Many Requests)
                if (peticiones > 5) {
                    return Response.status(429)
                            .entity(Map.of(
                                "error", "Límite de peticiones excedido.",
                                "mensaje", "Has superado el límite de 5 enlaces acortados por minuto. Por favor, intenta más tarde."
                            ))
                            .type(MediaType.APPLICATION_JSON)
                            .build();
                }
            }
        }

        return null; // Continuar con la ejecución normal de la petición
    }

    private String obtenerIpCliente(ContainerRequestContext contexto) {
        String xForwarded = contexto.getHeaderString("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isBlank()) {
            return xForwarded.split(",")[0].trim();
        }
        return "127.0.0.1";
    }
}
