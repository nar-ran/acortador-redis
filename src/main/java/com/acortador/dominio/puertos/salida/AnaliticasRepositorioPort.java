package com.acortador.dominio.puertos.salida;

import com.acortador.dominio.modelo.Analitica;
import java.util.Map;

// Puerto de salida para gestionar métricas, rankings y notificaciones en tiempo real con Redis.
public interface AnaliticasRepositorioPort {
    void registrarClic(String codigo, String navegador);
    Analitica obtenerAnaliticas(String codigo);
    Map<String, Long> obtenerRankingPopularidad(int limite);
    void publicarActividadClic(String codigo, String urlOriginal, String navegador);
}
