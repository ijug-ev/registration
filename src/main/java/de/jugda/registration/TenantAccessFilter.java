package de.jugda.registration;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Resolves the tenant from the {tenant} path parameter and grants access only
 * if that tenant is among the user's client roles (which Quarkus maps into
 * SecurityIdentity from resource_access/webapp/roles).
 *
 * <p>Runs after authentication (post-matching filter), so the SecurityIdentity
 * and the matched path parameters are both available. Requests to endpoints
 * without a {tenant} path segment are passed through untouched.
 */
@Provider
public class TenantAccessFilter implements ContainerRequestFilter {

    @Inject
    SecurityIdentity identity;

    @Inject
    TenantContext tenantContext;

    @Override
    public void filter(ContainerRequestContext ctx) {
        String tenant = ctx.getUriInfo().getPathParameters().getFirst("tenant");
        if (tenant == null) {
            return; // not a tenant-scoped endpoint -> nothing to enforce
        }

        // TODO extend for "public" pages, which don't require authentication,
        //      but still need to be accessible by all tenants.

        // Pure allow/deny: is this tenant in the user's set of tenants?
        if (!identity.getRoles().contains(tenant)) {
            ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                .entity("No access to tenant '" + tenant + "'")
                .build());
            return;
        }

        // Authorized -> make the tenant available for the rest of the request
        // (resource methods, and e.g. a Hibernate TenantResolver for data isolation).
        tenantContext.setTenantId(tenant);
    }
}
