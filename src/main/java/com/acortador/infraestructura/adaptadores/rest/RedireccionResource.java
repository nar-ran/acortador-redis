package com.acortador.infraestructura.adaptadores.rest;

import com.acortador.aplicacion.puertos.entrada.AcortarUrlUseCase;
import com.acortador.dominio.excepciones.EnlaceNoEncontradoException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.net.URI;

@Path("/")
public class RedireccionResource {

    @Inject
    AcortarUrlUseCase acortarUseCase;

    @GET
    @Path("/{codigo}")
    public Response redireccionar(@PathParam("codigo") String codigo, @HeaderParam("User-Agent") String userAgent) {
        try {
            String navegador = identificarNavegador(userAgent);
            String urlOriginal = acortarUseCase.obtenerUrlOriginal(codigo, navegador);

            if (!urlOriginal.startsWith("http://") && !urlOriginal.startsWith("https://")) {
                urlOriginal = "https://" + urlOriginal;
            }

            return Response.temporaryRedirect(URI.create(urlOriginal)).build();
        } catch (EnlaceNoEncontradoException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("<h3>El enlace corto no existe o ha expirado.</h3>")
                    .type("text/html;charset=UTF-8")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("<h3>La URL de redirección no es válida.</h3>")
                    .type("text/html;charset=UTF-8")
                    .build();
        }
    }

    private String identificarNavegador(String userAgent) {
        if (userAgent == null) {
            return "Otros";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg")) {
            return "Edge";
        } else if (ua.contains("chrome") && !ua.contains("chromium")) {
            return "Chrome";
        } else if (ua.contains("firefox")) {
            return "Firefox";
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            return "Safari";
        }
        return "Otros";
    }
}
