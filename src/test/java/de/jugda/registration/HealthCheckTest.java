package de.jugda.registration;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * The health endpoints are the one part of the app that is polled by machines instead of people,
 * so the two ways they break silently are pinned down here: a redirect instead of a status (OIDC
 * runs in web-app mode, where an accidentally secured path answers a probe with a 302 to Keycloak
 * that a naive monitor may even count as "reachable"), and a readiness check that reports UP
 * without checking anything.
 *
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
@QuarkusTest
public class HealthCheckTest {

    @Test
    void theHealthEndpointsAnswerAnonymouslyAndWithoutARedirect() {
        for (String path : new String[]{"/q/health", "/q/health/live", "/q/health/ready", "/q/health/started"}) {
            given()
                .redirects().follow(false)
            .when()
                .get(path)
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
        }
    }

    @Test
    void readinessActuallyChecksTheDatabase() {
        given()
        .when()
            .get("/q/health/ready")
        .then()
            .statusCode(200)
            // The check is contributed by quarkus-agroal, not by this code base, and disabling it
            // (quarkus.datasource.health.enabled=false) would leave a readiness probe that reports
            // UP while testing nothing. Its name is a SmallRye-internal string, so only its
            // presence is asserted, not its wording.
            .body("checks", hasSize(greaterThanOrEqualTo(1)))
            .body("checks[0].status", equalTo("UP"));
    }
}
