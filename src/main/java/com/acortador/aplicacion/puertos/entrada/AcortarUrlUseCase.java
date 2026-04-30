package com.acortador.aplicacion.puertos.entrada;

import com.acortador.dominio.modelo.Enlace;

public interface AcortarUrlUseCase {
    Enlace acortar(String urlOriginal, Long tiempoVidaSegundos);
    String obtenerUrlOriginal(String codigo, String navegador);
}
