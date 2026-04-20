package com.acortador.aplicacion.servicios;

import com.acortador.aplicacion.puertos.entrada.ObtenerAnaliticasUseCase;
import com.acortador.dominio.modelo.Analitica;
import com.acortador.dominio.puertos.salida.AnaliticasRepositorioPort;
import java.util.Map;

// Servicio puro para consultar estadísticas de clics y rankings acumulados en Redis.
public class ObtenerAnaliticasService implements ObtenerAnaliticasUseCase {

    private final AnaliticasRepositorioPort analiticasRepo;

    public ObtenerAnaliticasService(AnaliticasRepositorioPort analiticasRepo) {
        this.analiticasRepo = analiticasRepo;
    }

    @Override
    public Analitica consultar(String codigo) {
        return analiticasRepo.obtenerAnaliticas(codigo);
    }

    @Override
    public Map<String, Long> consultarRanking(int limite) {
        return analiticasRepo.obtenerRankingPopularidad(limite);
    }
}
