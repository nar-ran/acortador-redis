package com.acortador.infraestructura.configuracion;

import com.acortador.aplicacion.puertos.entrada.AcortarUrlUseCase;
import com.acortador.aplicacion.puertos.entrada.ObtenerAnaliticasUseCase;
import com.acortador.aplicacion.servicios.AcortarUrlService;
import com.acortador.aplicacion.servicios.ObtenerAnaliticasService;
import com.acortador.dominio.puertos.salida.AnaliticasRepositorioPort;
import com.acortador.dominio.puertos.salida.EnlaceRepositorioPort;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@Dependent
public class BeanConfiguration {

    @Produces
    @Singleton
    public AcortarUrlUseCase acortarUrlUseCase(
            EnlaceRepositorioPort enlaceRepo, 
            AnaliticasRepositorioPort analiticasRepo) {
        return new AcortarUrlService(enlaceRepo, analiticasRepo);
    }

    @Produces
    @Singleton
    public ObtenerAnaliticasUseCase obtenerAnaliticasUseCase(
            AnaliticasRepositorioPort analiticasRepo) {
        return new ObtenerAnaliticasService(analiticasRepo);
    }
}
