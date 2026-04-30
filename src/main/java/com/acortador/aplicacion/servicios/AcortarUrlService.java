package com.acortador.aplicacion.servicios;

import com.acortador.aplicacion.puertos.entrada.AcortarUrlUseCase;
import com.acortador.dominio.excepciones.EnlaceNoEncontradoException;
import com.acortador.dominio.modelo.Enlace;
import com.acortador.dominio.puertos.salida.AnaliticasRepositorioPort;
import com.acortador.dominio.puertos.salida.EnlaceRepositorioPort;
import java.util.Optional;
import java.util.UUID;

public class AcortarUrlService implements AcortarUrlUseCase {

    private final EnlaceRepositorioPort enlaceRepo;
    private final AnaliticasRepositorioPort analiticasRepo;

    public AcortarUrlService(EnlaceRepositorioPort enlaceRepo, AnaliticasRepositorioPort analiticasRepo) {
        this.enlaceRepo = enlaceRepo;
        this.analiticasRepo = analiticasRepo;
    }

    @Override
    public Enlace acortar(String urlOriginal, Long tiempoVidaSegundos) {
        String codigo = generarCodigoUnico();
        Enlace enlace = new Enlace(codigo, urlOriginal, tiempoVidaSegundos);
        
        enlaceRepo.guardar(enlace);
        return enlace;
    }

    @Override
    public String obtenerUrlOriginal(String codigo, String navegador) {
        Optional<Enlace> enlaceOpt = enlaceRepo.obtenerPorCodigo(codigo);
        if (enlaceOpt.isEmpty()) {
            throw new EnlaceNoEncontradoException("El enlace con código '" + codigo + "' no existe o ha expirado.");
        }

        Enlace enlace = enlaceOpt.get();
        
        analiticasRepo.registrarClic(codigo, navegador);
        analiticasRepo.publicarActividadClic(codigo, enlace.getUrlOriginal(), navegador);

        return enlace.getUrlOriginal();
    }

    private String generarCodigoUnico() {
        String codigo;
        do {
            codigo = UUID.randomUUID().toString().substring(0, 6);
        } while (enlaceRepo.existeCodigo(codigo));
        return codigo;
    }
}
