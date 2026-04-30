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

        comandosHash.hincrby(claveAnaliticas, "total", 1L);
        comandosHash.hincrby(claveAnaliticas, navegador.toLowerCase(), 1L);
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
        
        Map<String, Long> clicsPorNavegador = new HashMap<>(datos);
        clicsPorNavegador.remove("total");

        return new Analitica(codigo, clicsTotales, clicsPorNavegador);
    }

    @Override
    public Map<String, Long> obtenerRankingPopularidad(int limite) {
        List<ScoredValue<String>> ranking = comandosZSet.zrangeWithScores("enlaces:ranking", -limite, -1);
        java.util.Collections.reverse(ranking);
        
        Map<String, Long> resultado = new LinkedHashMap<>();
        for (ScoredValue<String> sv : ranking) {
            resultado.put(sv.value(), (long) sv.score());
        }
        return resultado;
    }

    @Override
    public void publicarActividadClic(String codigo, String urlOriginal, String navegador) {
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
