package com.acortador.aplicacion.puertos.entrada;

import com.acortador.dominio.modelo.Analitica;
import java.util.Map;

// Caso de uso para obtener métricas de clics y rankings de popularidad.
public interface ObtenerAnaliticasUseCase {
    Analitica consultar(String codigo);
    Map<String, Long> consultarRanking(int limite);
}
