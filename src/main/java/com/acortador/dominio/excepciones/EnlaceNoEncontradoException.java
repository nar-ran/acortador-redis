package com.acortador.dominio.excepciones;

// Excepción que se lanza cuando intentamos buscar un código que no existe en Redis.
public class EnlaceNoEncontradoException extends RuntimeException {
    public EnlaceNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
