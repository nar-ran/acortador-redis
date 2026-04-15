package com.acortador.infraestructura.adaptadores.redis;

import com.acortador.dominio.modelo.Analitica;
import com.acortador.dominio.puertos.salida.AnaliticasRepositorioPort;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import io.quarkus.redis.datasource.sortedset.ScoredValue;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Adaptador que conecta el puerto de analíticas con Redis usando Hashes, Sorted Sets y Pub/Sub.
@ApplicationScoped
public class RedisAnaliticasAdapter implements AnaliticasRepositorioPort {

    private final HashCommands<String, String, Long> comandosHash;
    private final SortedSetCommands<String, String> comandosZSet;
    private final PubSubCommands<String> comandosPubSub;

    @Inject
    public RedisAnaliticasAdapter(RedisDataSource ds) {
        this.comandosHash = ds.hash(Long.class);
        this.comandosZSet = ds.sortedSet(String.class);
        this.comandosPubSub = ds.pubsub(String.class);
    }

    @Override
    public void registrarClic(String codigo, String navegador) {
        String claveAnaliticas = obtenerClaveAnaliticas(codigo);

        // 1. Incrementamos los clics totales en el Hash del enlace
        comandosHash.hincrby(claveAnaliticas, "total", 1L);

        // 2. Incrementamos los clics para el navegador específico
        comandosHash.hincrby(claveAnaliticas, navegador.toLowerCase(), 1L);

        // 3. Incrementamos la popularidad en el ranking global (Sorted Set)
        comandosZSet.zincrby("enlaces:ranking", 1, codigo);
    }

    @Override
    public Analitica obtenerAnaliticas(String codigo) {
        String claveAnaliticas = obtenerClaveAnaliticas(codigo);
        Map<String, Long> datos = comandosHash.hgetall(claveAnaliticas);

        if (datos.isEmpty()) {
            return new Analitica(codigo, 0, new HashMap<>());
        }

        long clicsTotales = datos.getOrDefault("total", 0L);
        
        // Creamos un mapa exclusivo para los navegadores sin el campo 'total'
        Map<String, Long> clicsPorNavegador = new HashMap<>(datos);
        clicsPorNavegador.remove("total");

        return new Analitica(codigo, clicsTotales, clicsPorNavegador);
    }

    @Override
    public Map<String, Long> obtenerRankingPopularidad(int limite) {
        // Obtenemos los elementos con sus respectivos puntajes de mayor a menor
        List<ScoredValue<String>> ranking = comandosZSet.zrevrangeWithScores("enlaces:ranking", 0, limite - 1);
        
        // Usamos LinkedHashMap para mantener el orden de inserción de mayor a menor clics
        Map<String, Long> resultado = new LinkedHashMap<>();
        for (ScoredValue<String> sv : ranking) {
            resultado.put(sv.value(), (long) sv.score());
        }
        return resultado;
    }

    @Override
    public void publicarActividadClic(String codigo, String urlOriginal, String navegador) {
        // Construimos un JSON simple para publicar en el canal
        String mensajeJson = String.format(
            "{\"codigo\":\"%s\",\"urlOriginal\":\"%s\",\"navegador\":\"%s\",\"fecha\":%d}",
            codigo, urlOriginal, navegador, System.currentTimeMillis()
        );
        comandosPubSub.publish("enlaces:actividad", mensajeJson);
    }

    private String obtenerClaveAnaliticas(String codigo) {
        return "enlace:" + codigo + ":analiticas";
    }
}
