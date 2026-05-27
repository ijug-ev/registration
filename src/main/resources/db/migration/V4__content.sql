create table public.content
(
    tenant varchar(255) not null,
    key    varchar(255) not null,
    value  text         not null,
    constraint content_pk
        primary key (tenant, key)
);

insert into public.content (tenant, key, value) values ('jugda', 'registration.name', 'Ohne Deinen Namen wissen wir nicht');
insert into public.content (tenant, key, value) values ('jugda', 'registration.email', 'Deine E-Mail Adresse nutzen wir');
insert into public.content (tenant, key, value) values ('jugda', 'registration.video', 'Sofern ich nicht möchte');
insert into public.content (tenant, key, value) values ('jugda', 'registration.disclaimer', 'Mit Deiner Anmeldung bestätigst Du, dass Du die Hinweise an den Eingabefeldern gelesen und verstanden hast und Du damit einverstanden bist. Wir geben Deine persönlichen Daten aus dieser Anmeldung grundsätzlich nicht weiter und löschen diese ca. 1 Woche nach der Veranstaltung.');
insert into public.content (tenant, key, value) values ('jugda', 'registration.waitlist', 'Für diese Veranstaltung sind zur Zeit alle verfügbaren Plätze belegt. Du kannst Dich aber auf der Warteliste eintragen. Wir informieren Dich dann');
insert into public.content (tenant, key, value) values ('jugda', 'webinar.tools', 'Wir nutzen für Videokonferenzen und Online-Meetings die Plattform von Zoom Video Communications');
insert into public.content (tenant, key, value) values ('cyberland', 'registration.name', 'Ohne Deinen Namen wissen wir nicht');
insert into public.content (tenant, key, value) values ('cyberland', 'registration.email', 'Deine E-Mail Adresse nutzen wir');
insert into public.content (tenant, key, value) values ('cyberland', 'registration.video', 'Sofern ich nicht möchte');
insert into public.content (tenant, key, value) values ('cyberland', 'registration.disclaimer', 'Mit Deiner Anmeldung bestätigst Du');
insert into public.content (tenant, key, value) values ('cyberland', 'registration.waitlist', 'Für diese Veranstaltung sind zur Zeit alle verfügbaren Plätze belegt. Du kannst Dich aber auf der Warteliste eintragen. Wir informieren Dich dann');
insert into public.content (tenant, key, value) values ('cyberland', 'webinar.tools', 'Wir nutzen für Veranstaltungen verschiedene Plattform. Mit Eurer Einwahl in die jeweilige Videokoferenz-Lösung akzeptiert Ihr unsere Datenschutzbedingungen:');
