package com.acortador.infraestructura.adaptadores.redis;

import com.acortador.dominio.modelo.Enlace;
import com.acortador.dominio.puertos.salida.EnlaceRepositorioPort;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;

// Adaptador que conecta nuestro puerto de salida de enlaces con Redis usando Strings de clave-valor.
@ApplicationScoped
public class RedisEnlaceAdapter implements EnlaceRepositorioPort {

    private final ValueCommands<String, String> comandosValor;
    private final KeyCommands<String> comandosClave;

    @Inject
    public RedisEnlaceAdapter(RedisDataSource ds) {
        this.comandosValor = ds.value(String.class);
        this.comandosClave = ds.key();
    }

    @Override
    public void guardar(Enlace enlace) {
        String clave = obtenerClave(enlace.getCodigo());
        // Guardamos la URL original bajo la clave "enlace:{codigo}"
        comandosValor.set(clave, enlace.getUrlOriginal());
        
        // Si el usuario configuró expiración, le aplicamos el TTL a la clave
        if (enlace.tieneExpiracion()) {
            comandosClave.expire(clave, enlace.getTiempoVidaSegundos());
        }
    }

    @Override
    public Optional<Enlace> obtenerPorCodigo(String codigo) {
        String clave = obtenerClave(codigo);
        String urlOriginal = comandosValor.get(clave);
        if (urlOriginal == null) {
            return Optional.empty();
        }
        
        // Consultamos el tiempo de vida restante para retornarlo en el modelo
        long ttl = comandosClave.ttl(clave);
        Enlace enlace = new Enlace(codigo, urlOriginal, ttl > 0 ? ttl : null);
        return Optional.of(enlace);
    }

    @Override
    public boolean existeCodigo(String codigo) {
        String clave = obtenerClave(codigo);
        return comandosClave.exists(clave);
    }

    private String obtenerClave(String codigo) {
        return "enlace:" + codigo;
    }
}
