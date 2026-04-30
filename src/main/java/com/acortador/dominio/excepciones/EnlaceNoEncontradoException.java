package com.acortador.dominio.excepciones;

public class EnlaceNoEncontradoException extends RuntimeException {
    public EnlaceNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
