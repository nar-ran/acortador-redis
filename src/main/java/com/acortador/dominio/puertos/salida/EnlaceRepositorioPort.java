package com.acortador.dominio.puertos.salida;

import com.acortador.dominio.modelo.Enlace;
import java.util.Optional;

public interface EnlaceRepositorioPort {
    void guardar(Enlace enlace);
    Optional<Enlace> obtenerPorCodigo(String codigo);
    boolean existeCodigo(String codigo);
}
