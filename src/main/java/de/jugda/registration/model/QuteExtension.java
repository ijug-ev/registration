package de.jugda.registration.model;

import io.quarkus.qute.TemplateExtension;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
public class QuteExtension {

    @TemplateExtension
    static boolean containsKey(Map<?, ?> map, Object key) {
        return map.containsKey(key);
    }

    @TemplateExtension
    static String gravatarUrl(JsonWebToken token) {
        String email = token.getClaim("email");
        if (email == null || email.isBlank()) {
            return "https://www.gravatar.com/avatar/?d=mp&s=32";
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
            return "https://www.gravatar.com/avatar/?d=mp&s=32";
        }
    }

}
