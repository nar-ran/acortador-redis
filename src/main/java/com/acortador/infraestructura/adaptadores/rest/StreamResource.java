package com.acortador.infraestructura.adaptadores.rest;

import com.acortador.infraestructura.adaptadores.redis.RedisPubSubAdapter;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Expone un canal de transmisión en vivo (Server-Sent Events) conectado directamente al Pub/Sub de Redis.
@Path("/api/live")
public class StreamResource {

    @Inject
    RedisPubSubAdapter pubsubAdapter;

    @GET
    @Path("/clicks")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<String> transmitirClicsEnVivo() {
        // Retorna el flujo reactivo de mensajes de Redis hacia el navegador
        return pubsubAdapter.escucharActividad();
    }
}
