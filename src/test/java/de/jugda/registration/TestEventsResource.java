package de.jugda.registration;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/test-events.json")
@Produces(MediaType.APPLICATION_JSON)
public class TestEventsResource {

    @GET
    public String getEvents() {
        String date = FunctionalTestBase.EVENT_ID;
        String uid = date.replace("-", "") + "@testjug";
        int year = FunctionalTestBase.TEST_EVENT_DATE.getYear();
        int month = FunctionalTestBase.TEST_EVENT_DATE.getMonthValue();

        return """
            [
              {
                "uid": "%s",
                "summary": "Testtalk (John Doe)",
                "title": "Testtalk",
                "description": "Lorem ipsum...",
                "speaker": "John Doe",
                "location": "Online",
                "url": "http://localhost:8081/%d/%02d/testtalk/",
                "start": "%sT18:30:00",
                "end": "%sT20:30:00",
                "timezone": "Europe/Berlin",
                "hideRegistration": false,
                "canceled": false,
                "externalEvent": false
              }
            ]""".formatted(uid, year, month, date, date);
    }
}
