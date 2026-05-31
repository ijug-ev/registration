package de.jugda.registration;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
@QuarkusTest
@TestSecurity(user = "alice", roles = {"test"})
public class AdminFunctionalTest extends FunctionalTestBase {

    @BeforeAll
    static void createParticipants() {
        int port = ConfigProvider.getConfig().getValue("quarkus.http.test-port", Integer.class);
        PARTICIPANTS.forEach(participant -> given().port(port).contentType(ContentType.URLENC)
            .formParams("eventId", EVENT_ID, "name", participant.getName(), "email", participant.getEmail(), "limit", 60)
            .post("/registration/" + TENANT).then().statusCode(200));
    }

    @Test
    void testEventsOverview() {
        given().get("/admin/" + TENANT + "/events")
            .then()
            .statusCode(200)
            .body("html.body.div.h2", equalTo("Event-Anmeldungen"))
            .body("html.body.div.table.tbody.tr.size()", is(1)) // 1 Event
        ;
    }

    @Test
    void testEventRegistrations() {
        given().accept(ContentType.HTML)
            .pathParam("eventId", EVENT_ID)
            .get("/admin/" + TENANT + "/events/{eventId}")
            .then()
            .statusCode(200)
            .body(containsString("Anmeldungen für Event am"))
            .body("html.body.div.table.tbody.tr.size()", is(PARTICIPANTS.size())) // all participants should be registered
        ;
    }

    // Upload additional webinar data to event
    @Test
    void testUploadEventData() {
        given().contentType(ContentType.JSON)
            .pathParam("eventId", EVENT_ID)
            .body("{\"webinarLink\" : \"https://example.com/webinar\"}")
            .put("/admin/" + TENANT + "/events/{eventId}/data")
            .then()
            .statusCode(204);
    }

    @Test
    void testSendBulkEmailToParticipants() {
        given().contentType(ContentType.JSON)
            .pathParam("eventId", EVENT_ID)
            .body("{\"subject\" : \"Test Event\", \"summary\" : \"Herzlich willkommen\", \"registrationIds\" : []}")
            .put("/admin/" + TENANT + "/events/{eventId}/message")
            .then()
            .statusCode(204);
    }

    @Test
    void testWebinarPage() {
        given()
            .get("/webinar/" + TENANT + "/" + EVENT_ID)
            .then()
            .statusCode(200)
            .body("html.body.div.div[1].div.h3", equalTo("Link zu unserem Online-Meeting"))
        ;
    }
}
