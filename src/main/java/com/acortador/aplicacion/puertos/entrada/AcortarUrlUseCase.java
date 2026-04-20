package com.acortador.aplicacion.puertos.entrada;

import com.acortador.dominio.modelo.Enlace;

// Caso de uso para acortar una URL original y generar un código único.
public interface AcortarUrlUseCase {
    Enlace acortar(String urlOriginal, Long tiempoVidaSegundos);
    String obtenerUrlOriginal(String codigo, String navegador);
}
