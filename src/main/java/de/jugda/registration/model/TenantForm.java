package de.jugda.registration.model;

import jakarta.ws.rs.FormParam;
import lombok.Data;

@Data
public class TenantForm {
    @FormParam("name")
    public String name;
    @FormParam("website")
    public String website;
    @FormParam("privacy")
    public String privacy;
    @FormParam("imprint")
    public String imprint;
    @FormParam("logo")
    public String logo;
    @FormParam("replyTo")
    public String replyTo;
    @FormParam("events")
    public String events;
}
