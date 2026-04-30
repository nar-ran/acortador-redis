package com.acortador.infraestructura.adaptadores.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

// Pruebas de integración de Quarkus utilizando RestAssured.
// Al ejecutar esta clase, Quarkus iniciará el servidor de desarrollo y levantará de forma autónoma el Redis embebido.
@QuarkusTest
public class EnlaceResourceTest {

    @Test
    public void testCrearYRedireccionarEnlace() {
        // 1. Crear un enlace acortado
        var respuesta = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "urlOriginal", "https://google.com",
                        "tiempoVidaSegundos", 3600
                ))
                .when()
                .post("/api/enlaces")
                .then()
                .statusCode(201)
                .body("codigo", notNullValue())
                .body("urlOriginal", is("https://google.com"))
                .extract();

        String codigo = respuesta.path("codigo");

        // 2. Verificar que la redirección HTTP 302 apunte a la URL original
        given()
                .redirects().follow(false) // Detener la redirección para validar los headers HTTP
                .when()
                .get("/" + codigo)
                .then()
                .statusCode(307)
                .header("Location", is("https://google.com"));
    }

    @Test
    public void testEnlaceNoEncontrado() {
        // Verificar que un enlace inexistente devuelva un HTTP 404
        given()
                .when()
                .get("/noexist")
                .then()
                .statusCode(404);
    }
}
