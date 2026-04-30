package com.acortador.infraestructura.adaptadores.redis;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.pubsub.ReactivePubSubCommands;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RedisPubSubAdapter {

    private final ReactivePubSubCommands<String> pubsubReactivo;

    @Inject
    public RedisPubSubAdapter(ReactiveRedisDataSource ds) {
        this.pubsubReactivo = ds.pubsub(String.class);
    }

    public Multi<String> escucharActividad() {
        return pubsubReactivo.subscribe("enlaces:actividad");
    }
}
