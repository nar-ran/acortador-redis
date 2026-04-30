package com.acortador.infraestructura.configuracion;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.embedded.RedisServer;

// Servidor de Redis embebido para perfiles de desarrollo y pruebas.
@ApplicationScoped
public class RedisLocalDevServer {

    @Inject
    @ConfigProperty(name = "quarkus.profile")
    String perfil;

    private RedisServer servidorRedis;

    void alIniciar(@Observes StartupEvent evento) {
        if ("dev".equals(perfil) || "test".equals(perfil)) {
            try {
                System.out.println("[INFO] Iniciando servidor Redis embebido en el puerto 6379...");
                servidorRedis = new RedisServer(6379);
                servidorRedis.start();
                System.out.println("[INFO] Servidor Redis embebido en memoria iniciado exitosamente en puerto 6379.");
            } catch (Exception e) {
                System.err.println("[ADVERTENCIA] No se pudo iniciar el servidor Redis embebido: " + e.getMessage());
                System.err.println("[ADVERTENCIA] Si ya tienes una instancia de Redis corriendo localmente, esta se utilizará por defecto.");
            }
        }
    }

    void alDetener(@Observes io.quarkus.runtime.ShutdownEvent evento) {
        if (servidorRedis != null) {
            System.out.println("[INFO] Deteniendo servidor Redis embebido...");
            try {
                servidorRedis.stop();
                System.out.println("[INFO] Servidor Redis embebido detenido.");
            } catch (Exception e) {
                System.err.println("[ERROR] Error al detener Redis embebido: " + e.getMessage());
            }
        }
    }
}
