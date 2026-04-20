package com.acortador.infraestructura.adaptadores.rest;

import com.acortador.aplicacion.puertos.entrada.AcortarUrlUseCase;
import com.acortador.aplicacion.puertos.entrada.ObtenerAnaliticasUseCase;
import com.acortador.dominio.modelo.Analitica;
import com.acortador.dominio.modelo.Enlace;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

// Endpoint principal para crear enlaces y consultar analíticas y rankings.
@Path("/api/enlaces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnlaceResource {

    @Inject
    AcortarUrlUseCase acortarUseCase;

    @Inject
    ObtenerAnaliticasUseCase analiticasUseCase;

    @POST
    public Response crear(CrearEnlaceRequest peticion) {
        if (peticion.urlOriginal() == null || peticion.urlOriginal().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "La URL original es obligatoria"))
                    .build();
        }

        Enlace enlace = acortarUseCase.acortar(peticion.urlOriginal().trim(), peticion.tiempoVidaSegundos());
        String urlCorta = "http://localhost:8080/" + enlace.getCodigo();

        return Response.status(Response.Status.CREATED)
                .entity(new EnlaceRespuesta(enlace.getCodigo(), enlace.getUrlOriginal(), urlCorta, enlace.getTiempoVidaSegundos()))
                .build();
    }

    @GET
    @Path("/{codigo}/analiticas")
    public Response obtenerAnaliticas(@PathParam("codigo") String codigo) {
        Analitica analitica = analiticasUseCase.consultar(codigo);
        return Response.ok(analitica).build();
    }

    @GET
    @Path("/ranking")
    public Response obtenerRanking(@QueryParam("limite") @DefaultValue("5") int limite) {
        Map<String, Long> ranking = analiticasUseCase.consultarRanking(limite);
        return Response.ok(ranking).build();
    }

    // DTOs auxiliares
    public record CrearEnlaceRequest(String urlOriginal, Long tiempoVidaSegundos) {}
    public record EnlaceRespuesta(String codigo, String urlOriginal, String urlCorta, Long tiempoVidaSegundos) {}
}
