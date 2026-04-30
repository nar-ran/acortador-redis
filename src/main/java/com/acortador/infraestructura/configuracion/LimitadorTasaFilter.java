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

        if ("api/enlaces".equals(ruta) || "/api/enlaces".equals(ruta)) {
            if ("POST".equalsIgnoreCase(metodo)) {
                String ipCliente = obtenerIpCliente(contexto);
                String claveLimite = "limite:ip:" + ipCliente;

                Long peticiones = comandosValor.incr(claveLimite);

                if (peticiones == 1) {
                    comandosClave.expire(claveLimite, 60);
                }

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

        return null;
    }

    private String obtenerIpCliente(ContainerRequestContext contexto) {
        String xForwarded = contexto.getHeaderString("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isBlank()) {
            return xForwarded.split(",")[0].trim();
        }
        return "127.0.0.1";
    }
}
