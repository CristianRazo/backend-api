package com.crisscript;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class GreetingResourceTest {

    @Test
    public void testDealsEndpointExists() {
        // Verificamos que el endpoint responda, aunque sea un 415 (Unsupported Media Type)
        // porque no le estamos enviando un JSON, pero esto confirma que la app inició y el path existe.
        given()
          .when().post("/v1/deals/filter")
          .then()
             .statusCode(415); 
    }
}