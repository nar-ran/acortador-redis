package com.acortador.dominio.modelo;

import java.util.HashMap;
import java.util.Map;

// Guarda las estadísticas de visitas y clics de un enlace acortado.
public class Analitica {
    private String codigo;
    private long clicsTotales;
    private Map<String, Long> clicsPorNavegador;

    public Analitica() {
        this.clicsPorNavegador = new HashMap<>();
    }

    public Analitica(String codigo, long clicsTotales, Map<String, Long> clicsPorNavegador) {
        this.codigo = codigo;
        this.clicsTotales = clicsTotales;
        this.clicsPorNavegador = clicsPorNavegador != null ? clicsPorNavegador : new HashMap<>();
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public long getClicsTotales() {
        return clicsTotales;
    }

    public void setClicsTotales(long clicsTotales) {
        this.clicsTotales = clicsTotales;
    }

    public Map<String, Long> getClicsPorNavegador() {
        return clicsPorNavegador;
    }

    public void setClicsPorNavegador(Map<String, Long> clicsPorNavegador) {
        this.clicsPorNavegador = clicsPorNavegador;
    }
}
