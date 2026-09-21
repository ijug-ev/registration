package de.jugda.registration;

import io.quarkus.oidc.IdToken;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Exposes the logged-in user to the templates, reachable as {@code inject:currentUser}.
 * Named CDI bean rather than per-page template data: the menu is included on every admin page, so name,
 * avatar and role flag would otherwise have to be threaded through every single resource method.
 * <p>
 * The admin flag only governs what the navigation shows. The endpoints stay guarded by
 * {@code @RolesAllowed} -- hiding a link is a courtesy, not a permission check.
 */
@Named("currentUser")
@RequestScoped
public class CurrentUser {

    private static final String ANONYMOUS_AVATAR = "https://www.gravatar.com/avatar/?d=mp&s=32";

    @Inject
    SecurityIdentity identity;

    @Inject
    @IdToken
    JsonWebToken idToken;

    public boolean isAdmin() {
        return identity.hasRole("admin");
    }

    public String getName() {
        return idToken.getClaim("name");
    }

    /**
     * Gravatar addresses the avatar by the MD5 hash of the mail address -- that is the service's
     * protocol, not a security decision.
     */
    public String getGravatarUrl() {
        String email = idToken.getClaim("email");
        if (email == null || email.isBlank()) {
            return ANONYMOUS_AVATAR;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return "https://www.gravatar.com/avatar/" + hex + "?d=mp&s=32";
        } catch (NoSuchAlgorithmException e) {
            return ANONYMOUS_AVATAR;
        }
    }
}
