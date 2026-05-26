package de.jugda.registration;

import jakarta.enterprise.context.RequestScoped;
import lombok.Getter;
import lombok.Setter;

/**
 * Request-scoped holder for the currently active tenant.
 * Set by {@link TenantAccessFilter} once access has been authorized,
 * and read downstream (resource methods, optional Hibernate TenantResolver).
 */
@RequestScoped
@Getter @Setter
public class TenantContext {
    private String tenantId;
}
