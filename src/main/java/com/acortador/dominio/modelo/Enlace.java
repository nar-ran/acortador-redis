package com.acortador.dominio.modelo;

import java.time.LocalDateTime;

// Representa un enlace acortado dentro del dominio de la aplicación.
public class Enlace {
    private String codigo;
    private String urlOriginal;
    private Long tiempoVidaSegundos; // TTL opcional
    private LocalDateTime fechaCreacion;

    public Enlace() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Enlace(String codigo, String urlOriginal, Long tiempoVidaSegundos) {
        this.codigo = codigo;
        this.urlOriginal = urlOriginal;
        this.tiempoVidaSegundos = tiempoVidaSegundos;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Verifica si el enlace tiene un tiempo de expiración definido.
    public boolean tieneExpiracion() {
        return tiempoVidaSegundos != null && tiempoVidaSegundos > 0;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getUrlOriginal() {
        return urlOriginal;
    }

    public void setUrlOriginal(String urlOriginal) {
        this.urlOriginal = urlOriginal;
    }

    public Long getTiempoVidaSegundos() {
        return tiempoVidaSegundos;
    }

    public void setTiempoVidaSegundos(Long tiempoVidaSegundos) {
        this.tiempoVidaSegundos = tiempoVidaSegundos;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
