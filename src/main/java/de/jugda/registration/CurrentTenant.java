package de.jugda.registration;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Exposes the current tenant to the admin templates, reachable as {@code inject:tenant}.
 * <p>
 * Every admin page shows the JUG's logo and name in the menu and links back to its own URLs, so the
 * tenant used to be threaded through every single resource method as template data -- and a resource
 * that forgot it answered with a 500. As a named bean it is simply always there, and Quarkus validates
 * {@code inject:tenant.x} against the getters below at build time instead of at render time.
 * <p>
 * Deliberately a small delegate and not the {@link de.jugda.registration.domain.Tenant} entity itself:
 * the chrome needs three fields, and that is the surface the templates should see. The participant
 * pages keep passing their own {@code tenant} data -- they render for a tenant, they do not run inside
 * one the way the admin area does.
 */
@Named("tenant")
@RequestScoped
public class CurrentTenant {

    @Inject
    TenantContext tenantCtx;

    public String getId() {
        return tenantCtx.getTenantId();
    }

    public String getName() {
        return tenantCtx.getTenant().getName();
    }

    public String getLogo() {
        return tenantCtx.getTenant().getLogo();
    }
}
