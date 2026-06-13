package eu.ijug.events.invitation;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "invite_config")
@Getter
@Setter
public class InviteConfig extends PanacheEntityBase {

    @Id
    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "mail_to")
    private String mailTo;

    @Column(name = "masto_url")
    private String mastoUrl;

    @Column(name = "masto_token")
    private String mastoToken;
}
