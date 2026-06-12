package de.jugda.registration.api.authenticated;

import de.jugda.registration.TenantContext;
import de.jugda.registration.domain.Tenant;
import de.jugda.registration.model.TenantForm;
import io.quarkus.oidc.IdToken;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("admin/{tenant}/data")
@Produces(MediaType.TEXT_HTML)
@Authenticated
public class AdminTenantResource {

    @Location("admin/data")
    Template data;

    @Inject
    TenantContext tenantCtx;

    @Inject
    @IdToken
    JsonWebToken idToken;

    @Context
    UriInfo uriInfo;

    @GET
    public TemplateInstance get() {
        return data
            .data("tenant", tenantCtx.getTenant())
            .data("id", idToken)
            .data("activeNav", "data");
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response post(@BeanParam TenantForm form) {
        Tenant tenant = tenantCtx.getTenant();
        tenant.setName(form.getName());
        tenant.setWebsite(form.getWebsite());
        tenant.setPrivacy(form.getPrivacy());
        tenant.setImprint(form.getImprint());
        tenant.setLogo(form.getLogo());
        tenant.setReplyTo(form.getReplyTo());
        tenant.setEvents(form.getEvents());
        return Response.status(Response.Status.FOUND)
            .location(uriInfo.getRequestUri())
            .build();
    }
}
