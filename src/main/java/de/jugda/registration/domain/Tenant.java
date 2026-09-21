package de.jugda.registration.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import de.jugda.registration.model.TenantForm;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "tenant")
@Getter @Setter
@ToString
public class Tenant extends PanacheEntityBase {
    @Id
    @TenantId
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private String website;
    private String privacy;
    private String imprint;
    private String logo;
    @Column(name = "reply_to")
    private String replyTo;
    private String events;
    /** Optional, tenant-owned stylesheet for the participant pages, see {@link de.jugda.registration.TenantStyle}. */
    @Column(columnDefinition = "text")
    private String css;

    /**
     * Applies the master data form, like {@link Registration#updateFrom} does for a registration:
     * the column list stays next to the columns. The id is not part of it -- it is the tenant key.
     */
    public void updateFrom(TenantForm form) {
        this.name = form.getName();
        this.website = form.getWebsite();
        this.privacy = form.getPrivacy();
        this.imprint = form.getImprint();
        this.logo = form.getLogo();
        this.replyTo = form.getReplyTo();
        this.events = form.getEvents();
        // The field is optional: an emptied textarea has to clear the column, not store a blank string
        this.css = form.getCss() == null || form.getCss().isBlank() ? null : form.getCss().strip();
    }
}
