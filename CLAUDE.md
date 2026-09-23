# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Multi-tenant event registration application for JUG (Java User Group) events. Built on Quarkus 3.x / Java 25 / PostgreSQL. Designed to be embedded in an `<iframe>` on JUG websites.

Live URL pattern: `https://registration.ijug.eu`

## Common Commands

```bash
# Start in dev mode (spins up PostgreSQL, Mailpit, Keycloak via DevServices automatically)
./mvnw compile quarkus:dev

# Build
./mvnw clean package

# Run all tests (uses Testcontainers for PostgreSQL)
./mvnw test

# Run a specific test class
./mvnw test -Dtest=RegistrationAndDeletionFunctionalTest

# Native build
./mvnw clean package -Pnative
```

### Java Version

The project tracks the **latest Java LTS** and nothing newer -- it belongs to a Java user group, so it
should show the version people are expected to run in production (issue #54). Currently **Java 25**.

Raising it means five places, not one: `maven.compiler.release` in `pom.xml`, the `java-version` of the
four CI workflows (`.github/workflows/` **and** `.forgejo/workflows/` -- the project is mirrored to
git.ijug.eu), the `ubi9/openjdk-<v>-runtime` base image in `src/main/docker/Dockerfile.jvm` and
`Dockerfile.legacy-jar`, plus the prerequisite in `README.adoc`.

Two things to know when the next LTS arrives:
- Since JDK 23 javac no longer finds annotation processors on the classpath by itself, which silently
  disables Lombok. `<proc>full</proc>` in the compiler plugin is what keeps it running -- do not drop it.
- Lombok warns about `sun.misc.Unsafe::objectFieldOffset` on JDK 25. Harmless today, but that method is
  slated for removal, so a future LTS will need a Lombok that has moved off it.

### Dev Mode URLs
- Registration form: http://localhost:8080/registration/test?eventId=2026-12-31&opensBeforeInMonths=8
- Admin UI: http://localhost:8080/admin/test/events (credentials: `alice` / `alice`)
- Mailpit UI: http://localhost:8080/q/dev-ui/quarkus-mailpit/mailpit-ui
- Keycloak dev server: http://localhost:8081
- Health: http://localhost:8080/q/health

## Architecture

### Multi-Tenancy
The app serves multiple JUGs from a single deployment. The tenant is always the first path segment after the resource type (e.g., `/registration/{tenant}`, `/admin/{tenant}/events`).

- **`TenantAccessFilter`** — JAX-RS `ContainerRequestFilter` that reads `{tenant}` from the path, enforces that the authenticated user's OIDC roles include that tenant name, then sets the resolved tenant on `TenantContext`.
- **`TenantContext`** — `@RequestScoped` CDI bean carrying the current `tenantId` and lazy-loading the `Tenant` entity.
- **Hibernate DISCRIMINATOR multi-tenancy** — `Registration`, `Content`, and `Tenant` entities use `@TenantId` so all DB queries are automatically scoped. `CurrentTenantResolver` feeds the tenant value from `TenantContext`.

Known tenants (seeded in `V3__tenant.sql`): `test`, `jugda`, `cyberland`.

New tenants are created at runtime by **`TenantProvisioningService`** (`GET/POST /admin/{tenant}/tenants`,
role `admin`), which clones the `test` tenant: the `tenant` row and all its `content` rows are copied, only
id and name come from the form. It uses **native SQL on purpose** — `Tenant` and `Content` carry `@TenantId`,
so every JPA query is filtered to the *current* request's tenant, which is exactly the isolation this one
operation has to cross. Keycloak is not touched: a new tenant is unreachable in the admin UI until someone
creates the client role of the same name by hand.

### Endpoints

| Path | Auth | Description |
|---|---|---|
| `GET /registration/{tenant}` | anonymous | Show registration form |
| `POST /registration/{tenant}` | anonymous | Submit registration |
| `GET/POST /registration/{tenant}/delete` | anonymous | Self-service deregistration |
| `DELETE /registration/{tenant}/delete?id=` | anonymous | Delete one registration by UUID (used by the admin UI and the mail link) |
| `GET /registration/{tenant}/ical/{eventId}` | anonymous | Download `.ics` calendar file |
| `GET /meeting/{tenant}/{eventId}` | anonymous | Meeting landing page (today-only in prod, and only once a `meetingLink` is stored) |
| `GET /admin/{tenant}/events` | OIDC | Admin overview of all events |
| `GET /admin/{tenant}/events/{eventId}` | OIDC | List registrations for one event (HTML, or JSON with `Accept: application/json`) [^1] |
| `GET/POST /admin/{tenant}/data` | OIDC | View and edit the tenant's master data |
| `GET/POST /admin/{tenant}/content` | OIDC | View and edit the tenant's help texts (`Content`) |
| `GET/POST /admin/{tenant}/tenants` | OIDC + role `admin` | Create a new JUG by cloning the `test` tenant |
| `GET /admin/{tenant}/logout` | OIDC | RP-initiated logout, returns to `/admin/{tenant}` |
| `GET /admin/{tenant}/logs` | OIDC + role `admin` | Tail the server log |
| `PUT /admin/{tenant}/events/{eventId}/data` | OIDC | Update event metadata |
| `PUT /admin/{tenant}/events/{eventId}/message` | OIDC | Send bulk email to participants |

[^1]: Two resource methods share that path. The JSON one carries `qs=0.9`, so a client that sends no
`Accept` header gets the page -- without it the runtime picks whichever method it discovered first, which
is not stable across JVM runs and failed the menu test in roughly one build in eight.

### Health Endpoints

`quarkus-smallrye-health` exposes `/q/health`, `/q/health/live`, `/q/health/ready` and
`/q/health/started` (issue #61). There is **no hand-written `HealthCheck` in this code base**: the
single check that shows up is the datasource readiness check `quarkus-agroal` contributes by itself.

- **The external events feed is deliberately not a health check.** `EventService` loads each tenant's
  events JSON from a third-party URL. A readiness probe that went red when someone else's server
  hiccups would pull a working app out of rotation, while the registration pages keep serving from the
  5-minute Caffeine cache. Feed failures belong in the log.
- **The endpoints are anonymous and sit on the main port**, which is what lets an external uptime
  monitor reach them. They therefore have to stay out of `quarkus.http.auth.permission`: with
  `quarkus.oidc.application-type=web-app` a secured probe path answers **302 to Keycloak**, and a
  monitor that only asks "did I get a response" never notices. `HealthCheckTest` pins that down
  (`redirects().follow(false)`), plus the fact that readiness carries at least one check -- without
  that, `quarkus.datasource.health.enabled=false` would leave a probe reporting UP while testing
  nothing.

`docker-compose.yml` consumes them: `pg_isready` on the database, `curl -f .../q/health/ready` on the
app (the `ubi9/openjdk-25-runtime` image has `curl`, but no `wget`), and the app now waits for
`condition: service_healthy` instead of a bare `depends_on: database` -- which used to let Flyway race
the database's first accepting connection. Compose does not restart an unhealthy container on its own;
the healthcheck buys ordering and visibility, not self-healing.

### Key Components

- **`RegistrationResource`** — handles registration form display and submission; decides between `registration`, `closed`, and `not_yet_open` templates based on deadline / `opensBeforeInMonths`.
- **`RegistrationService`** — persists a registration (upsert on eventId+email), triggers confirmation email.
- **`EventService`** — fetches events from an external JSON URL (per tenant, see `Tenant.events`), cached with Caffeine (`events` cache, 5-min TTL).
- **`EmailService`** — sends confirmation, waitlist-to-attendee, and bulk emails via Quarkus Mailer + Qute templates.
- **`CleanupJob`** — scheduled job that purges expired registrations (based on `ttl` epoch seconds, set to 1 week after the event).
- **`Content`** — tenant-scoped key/value texts stored in the `content` table; injected into templates via `Content.asMap()` as `helptext`.
- **`ContentKey`** — enum of the help-text keys the templates actually read. It is the single source of truth for the
  admin "Texte" form (`AdminContentResource` / `ContentService`): only these keys are rendered and saved, unknown
  form fields are dropped. `ContentKeyTemplateTest` scans the templates for `helptext["..."]` and fails in both
  directions — a lookup with no enum constant (nobody can fill it) and an enum constant no template renders
  (editing it does nothing).

### Data Model
- `Registration` — one row per participant per event; `ttl` auto-expires ~1 week post-event.
- `Tenant` — configuration per JUG: name, website, privacy/imprint URLs, logo, reply-to address, events JSON URL.
- `EventData` — mutable per-event key/value metadata, editable by admins.
- `Content` — tenant-scoped UI help texts (name field hint, email hint, video recording notice, etc.).

### Templates
Qute HTML templates in `src/main/resources/templates/`. Mail templates are in `templates/mail/`.

**Three layouts, no page-level chrome.** `template.html` is the base (html/head/body, Bootstrap, the
script slot, the height reporter). Above it sit two layouts, and a page includes exactly one of them:

| Layout | Used by | Adds |
|---|---|---|
| `public.html` | the 6 registration/deregistration pages | the tenant's own stylesheet |
| `meeting/layout.html` (on `public.html`) | the 2 meeting pages | container, logo header, page title |
| `admin/layout.html` | the 6 admin pages | container, menu, content column, heading, error alert |

A page fills `{#content}` (`{#body}` on the participant pages, which *are* the body) and passes what the
layout needs as include parameters: `heading="..."`, `nav="..."`, `colStyle="..."`. **A layout only names
the sections it actually fills.** Qute resolves an `{#insert}` up the entire include chain, so a page's
`{#body}`, `{#scripts}` or `{#title}` reaches `template.html` through any number of layouts that never
mention it -- forwarding blocks like `{#scripts}{#insert scripts}{/}{/scripts}` are pure no-ops.
A page whose heading is more than a title (`admin/list.html`) simply omits `heading` and writes its
own markup at the top of `{#content}`.

**Optional include parameters need `.or(...)`, optional data needs `??`.** Strict rendering is on: an
expression whose key nobody supplied is a 500, not an empty string. That is why the layout writes
`{colStyle.or('')}` and `{#if error??}`, and why `tags/field.html` guards its hint with
`{#if nested-content??}` -- for a `{#field ... /}` call without a body that key does not exist at all.

**`templates/tags/` holds user-defined tags**, registered automatically under their file name.
`tags/field.html` is one labelled row of an admin form, called as
`{#field name="website" label="Website" type="url" value=form.website /}`, with the hint as tag body.

**Page scripts belong in the `{#scripts}` block, not in `{#body}`.** `template.html` loads jQuery and Bootstrap at the end of `<body>` and then offers a `{#insert scripts}{/}` slot. A `<script>` inside `{#body}` is rendered before the library tags and therefore runs before `$` exists -- with no visible error, the page just stays dead.

**Inline JS with object literals needs a Qute raw block `{|` ... `|}`.** Qute parses
`{` as the start of an expression as soon as an identifier follows directly. A JS object literal
like `{type: 'x', height: h}` is therefore read as an expression and the page dies at runtime
with `No namespace resolver found for [type]`. The existing inline JS in `admin/list.html` only
works because every `{` there happens to be followed by a line break -- coincidence, not
protection. The height reporter in `template.html` is wrapped in `{|` ... `|}` accordingly.

`async`/`defer` are deliberately not set on the library tags: `defer` has no effect on inline scripts, so a deferred jQuery tag would still be overtaken by the slot scripts. Anyone who wants `defer` has to wrap every slot script in `DOMContentLoaded`.

### Database Migrations
Flyway migrations in `src/main/resources/db/migration/`. Run automatically at startup. Schema is managed solely via Flyway (`quarkus.hibernate-orm.schema-management.strategy=none`).

### Participant Mails via CDI Events (asynchronous)

No participant mail is sent inside the JTA transaction any more. The services only do DB work and
fire a CDI event from `de.jugda.registration.event` at the end:

- `RegistrationService.handleRegistration()` -> `RegistrationConfirmed`
- `DeleteService.processWaitlist()` -> `WaitlistPromoted`

Both are records `(tenantId, baseUrl, RegistrationDto)` and implement the sealed interface
`RegistrationEvent`. `EmailService` hangs exactly two observers off it -- **not one per event type,
but one on the supertype**, because CDI resolves observers through the runtime class and its
supertypes:

1. `relayAfterCommit(@Observes(during = AFTER_SUCCESS) RegistrationEvent)` -- runs only after the
   commit, so no mail goes out for a rolled-back transaction, and re-fires the same event via
   `fireAsync()`. `fireAsync` only reaches `@ObservesAsync` methods, so there is no loop.
2. `onRegistrationEvent(@ObservesAsync RegistrationEvent)` -- renders and sends the mail on a worker
   thread. Which template and which subject is decided by a `switch` over the sealed interface: a new
   event type therefore breaks compilation instead of silently sending no mail.

**Important for the async observer:** there is no HTTP request active there. `UriInfo` is therefore
unusable -- the base URL travels in the event payload. The request *context*, on the other hand, is
present: ArC activates it around every observer notification (`EventImpl.Notifier.notify`, as long as
`quarkus.arc.strict-compatibility` is off -- the default). So it does not have to be activated by
hand; the only thing that must be set is `TenantContext.tenantId` from the payload, otherwise
`CurrentTenantResolver`, `EventService` and `Tenant.findById()` come up empty. (There used to be a
manual `Arc.container().requestContext().activate()` here; a probe showed the context is already
active and the block never ran.) `@ActivateRequestContext` would be ineffective anyway, because
interceptors do not apply to observer methods.

Exceptions from asynchronous observers end up in a `CompletionStage` nobody inspects -- which is why
`onRegistrationEvent()` logs by itself. Consequence: a failed send no longer rolls the transaction
back, and the user sees the thank-you page regardless.

So that this does not stay invisible, `registration.confirmationSentAt` carries the state:

- `null` means *mail still pending* -- set when a registration is created or updated
  (`RegistrationService.handleRegistration`) and when someone moves up from the waitlist
  (`DeleteService.processWaitlist`).
- The timestamp is only set after a send that threw no exception, via
  `RegistrationService.markConfirmationSent(...)`. That has to be a **separate bean call**, otherwise
  the `@Transactional` interceptor does not apply (self-invocation), and there is no transaction on
  the worker thread anyway.
- `admin/list.html` shows an icon per row: a green envelope with a timestamp, otherwise a red one.
  A brief red right after a registration is normal -- the send is asynchronous.

Migration `V7` backfills existing rows with `created`: back then the send hung inside the transaction,
so a persisted row implies a delivered mail. Without the backfill every old registration would have
turned red after the deploy.

Test pitfall: assertions that read the DB need `pollInSameThread()`. Otherwise Awaitility polls on a
thread of its own, and `CurrentTenantResolver` is `@RequestScoped` -- the query dies with *no tenant
identifier specified*. Mailbox assertions are unaffected (`MockMailbox` is a singleton).

The bulk mail (`EmailService.sendBulkEmail`, admin UI) still runs synchronously in the request -- it
has no transaction problem and the admin wants to see the result right away. It issues *one*
`mailer.send(Mail...)` call per chunk of 50, which the mailer processes as a batch. The chunking lives
in `sendBulkEmail` itself (`Gatherers.windowFixed`), next to the send it exists for -- callers hand over
a plain list of recipients and cannot flatten it away by accident, which would send every mail
individually and blocking. The Qute `Fmt` of the bulk mail is deliberately **not** cached: Qute's
template cache is unbounded, and bulk mail texts are free-form admin input.

Tests: because the mails go out asynchronously, they have not necessarily arrived by the time the HTTP
response is back. `FunctionalTestBase.awaitTotalMails(n)` / `awaitMailsTo(mail, n)` poll for them with
Awaitility (`untilAsserted`) instead of asserting directly -- that way a timeout reports the number of
mails actually counted (`expected: 2 but was: 1`) and not merely that the wait expired.
The `MockMailbox` lives in `FunctionalTestBase`; both test classes use it.

### Tenant-owned CSS

`Tenant.css` (column `tenant.css`, migration `V8`) holds an optional stylesheet that the JUGs maintain
in the admin area under *JUG Data*, to match the iframe-embedded pages to their own website.

- **Only the participant pages get it, never the admin area.** A broken stylesheet must not break the
  very form used to repair it. Implemented as **opt-in through the layout**: `template.html` has an
  `{#insert styles}{/}` slot in `<head>` (behind Bootstrap, so the tenant's own rules win), and
  `public.html` -- the layout of every participant page -- is the only template that fills it. The
  `<style>` element is written there inline, deliberately not linked: these pages run in an iframe on
  someone else's site, where a second request only delays the first paint. The admin pages include
  `template.html` directly and
  stay unstyled by design. Opt-out would be shorter but would silently colour any admin page added later;
  it used to be opt-in per page, which meant every new participant page had to remember two lines.
- It is supplied by **`TenantStyle`**, a `@Named("tenantStyle") @RequestScoped` bean like `CurrentUser`,
  reachable as `inject:tenantStyle.present` / `inject:tenantStyle.css`. Not template data, because
  `tenant` means two different things across the participant templates -- the bare id string in
  `registration.html`/`thanks.html`, the `Tenant` entity in the meeting pages -- and `delete.html` gets
  no tenant at all. The bean loads the entity lazily during rendering; that works, the request context
  and the Hibernate session are still around at that point.
- `getCss()` deliberately returns a `RawString`: the CSS lands unescaped inside a `<style>` element.
  The only sequence that can break out of it is a literal `</style` (whitespace after the slash does
  *not* close the element). `AdminTenantResource.post` rejects it on save
  (`TenantStyle.closesTheStyleElement`), and `TenantStyle.load()` strips it again at render time, so a
  row that reached the database some other way cannot inject markup either.
- `admin/data.html` now renders its values from a `TenantForm` instead of straight off the entity --
  that is what lets a rejected POST hand the entered data back. `AdminTenantResource.post` stays
  `@Transactional` as a whole; a separate `save()` method would be self-invocation and the interceptor
  would not fire.
- `TenantProvisioningService` copies the column along -- its column list is maintained by hand.

### iframe Auto-Height

`template.html` ends with a height reporter: if the page is embedded
(`window.parent !== window`), a `ResizeObserver` reports the content height to the embedding
page via `postMessage` as `{type: 'ijug-registration:height', height: <px>}`. The message type
is a **public contract** with the JUG websites (snippet in `docs/handbuch.adoc`) -- renaming it
breaks their integration silently. A test in `RegistrationAndDeletionFunctionalTest` pins it
down.

`document.body` is measured deliberately, not `documentElement`: the document height grows to at
least the viewport height, which would keep the iframe from ever shrinking again after it grew
(feedback loop).

### CORS
`quarkus.http.cors.methods` in `application.properties` has to list every method the frontend uses -- currently `GET,POST,PUT,DELETE,OPTIONS`. The CORS filter runs **before** authentication and routing: a missing method is rejected with a **403 without a body**, which looks like a permission problem but is not one. Until 2026-08-19 `PUT` and `DELETE` were missing here, which made every writing admin function (saving event data, bulk mail, deleting a registration) fail silently in the browser.

### Authentication (OIDC / Keycloak)
- Production Keycloak: `https://id.ijug.eu/realms/ijug`
- Client ID: `registration`
- Roles are sourced from the access token at `resource_access/registration/roles`
- A role named exactly like the tenant ID grants admin access to that tenant
- Which nav entry is highlighted travels as an include parameter: a page passes `nav="data"` to
  `admin/layout.html`, which passes it on to the menu it includes. `admin/menu.html` binds
  `{#let active=nav.or('')}` around the nav list, because Qute renders strictly and the two operator
  pages highlight no entry at all -- without the default that would be a 500, not a blank highlight.
  `AdminFunctionalTest.testTheCurrentPageIsHighlightedInTheMenu` pins the chain down: a break in it
  leaves every entry unhighlighted instead of failing.
- **Every link in `admin/menu.html` is absolute (`/admin/{inject:tenant.id}/...`), not relative.** The menu is
  included by pages at two path depths: `/admin/{tenant}/events` and `/admin/{tenant}/events/{eventId}`
  (`admin/list.html`). On the detail page a relative `./events` resolved against `/admin/{tenant}/events/`
  and ended up at `/admin/{tenant}/events/events` -- a 404 instead of navigation. The remaining admin
  templates were absolute already.
  `AdminFunctionalTest.testMenuLinksAreAbsoluteOnTheEventDetailPage` pins this down.
- The two operator-only pages (*Neue JUG*, *Server-Logs*) live in the user dropdown at the bottom of
  `admin/menu.html`, wrapped in `{#if inject:currentUser.admin}`; the nav list above holds only the pages every
  orga team uses.
  `CurrentUser` is a `@Named @RequestScoped` bean; Qute resolves `inject:` namespace expressions against
  `@Named` beans and validates them at build time, which keeps the flag out of every single resource method.
  The endpoints stay guarded by `@RolesAllowed` — the hidden link is a courtesy, not a permission check.
- **The admin chrome reads its own data, the resources pass only what their page shows.** Besides
  `inject:currentUser` (admin flag, display name, gravatar URL) there is `inject:tenant`
  (`CurrentTenant`, delegating to `TenantContext`) for the logo, the name and the tenant id in every
  admin URL. Before, all of it was template data on every single resource method, where a forgotten
  key surfaced as a 500 at render time. The participant pages still pass their own `tenant` data --
  they render *for* a tenant instead of running inside one, and `tenant` means different things to
  them (id string in `registration.html`/`thanks.html`, entity in the meeting pages).
- Logout runs through `AdminLogoutResource` (`/admin/{tenant}/logout`), **not** through
  `quarkus.oidc.logout.path`: the built-in logout offers a single static `post-logout-path`, while the
  landing page has to carry the tenant. The resource assembles the RP-initiated logout request itself
  (`id_token_hint` + `post_logout_redirect_uri`) and clears the local session via `OidcSession.logout()`.
  The post-logout URI must be a valid post-logout redirect URI on the Keycloak client — the dev realm
  allows `*`, the production client at id.ijug.eu has to permit `https://registration.ijug.eu/admin/*`.
- Dev mode uses a local Keycloak DevService with `ijug-realm.json`

### Production Deployment
Deployed via Docker Compose (`docker-compose.yml`). Image: `ghcr.io/ijug-ev/registration:latest`. Built and pushed by GitHub Actions on pushes to `main` (tagged `latest`) or `v*` tags (tagged with version).
