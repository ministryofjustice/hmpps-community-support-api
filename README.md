# HMPPS Community Support API

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/hmpps-community-support-api/badge?style=flat)](https://github-community.service.justice.gov.uk/repository-standards/hmpps-community-support-api)
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-community-support-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://community-support-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)

This is the API repository for the Community Support team, being built for the 2027 changes to providers and processes around what was formerly Commissioned Rehabilitative Services (CRS).

This repo was generated from the Kotlin template repo used for new projects.

## Local Development

### Common commands

Lint:

`./gradlew ktlintCheck`

Format:

`./gradlew ktlintFormat`

Test:

`./gradlew clean test`

Build:

`./gradlew clean build`

### Running locally

The application comes with a `dev` spring profile that includes default settings for running locally. This is not
necessary when deploying to kubernetes as these values are included in the helm configuration templates -
e.g. `values-dev.yaml`.

`docker-compose.yml` only contains dependencies (HMPPS Auth and its own support services, postgres, wiremock,
localstack) - there is no app service, so `docker compose up` on its own does not run the API.

### Running the application in Intellij

```bash
docker compose up
```

starts HMPPS Auth and dependencies. Then run the app in Intellij with the `local` Spring profile active.

Add `127.0.0.1 hmpps-auth` to `/etc/hosts` so both your host and any containers resolve HMPPS Auth consistently
(the `local` profile talks to `http://hmpps-auth:8090/auth`).

### Running a full local stack including the UI

Two extra compose files bring up the [hmpps-community-support-ui](https://github.com/ministryofjustice/hmpps-community-support-ui)
app alongside the same dependencies, so you can develop across both repos entirely locally (API run in Intellij as above):

```bash
# build the UI from a sibling checkout at ../hmpps-community-support-ui
docker compose -f docker-compose-ui.yml up

# OR pull the latest published UI image instead of building from source
docker compose -f docker-compose-ui-ghcr.yml up
```

The UI is then available at http://localhost:3000, and reaches the Intellij-run API via `host.docker.internal:8080`.

Log in with the delius-sourced wiremock test user (grants `ROLE_PROBATION`):

username: `bernard.beaks`, password: `secret`

(`AUTH_USER`/`password123456` has no roles and will fail the UI's role check.)

## Creating local test users with custom roles

Once the stack is up, `scripts/local-user-setup.sh` creates a fully working local hmpps-auth
user (password set, no email step required) with whichever roles you need, entirely offline
(no GOV.UK Notify/real Delius/Nomis dependency):

```bash
./scripts/local-user-setup.sh [email] [ROLE1,ROLE2,...]
# defaults: local.tester@digital.justice.gov.uk / password123456 /
#           COMMUNITY_SUPPORT_REFERRER,COMMUNITY_SUPPORT_PROVIDER
```

It's safe to re-run this script (i.e. it won't re-create identical users). 
Every user it creates is assigned to the local Seetec provider group
(`INT_SP_SEETEC_BUS_TECH_CTR_LTD`), which maps to the seeded service provider
and enables provider-scoped journeys.

## Creating local appointment endpoint fixtures

With the API running using the `local` profile, create between one and ten submitted referral fixtures through the local-only admin endpoint:

```bash
# Create one fixture
./scripts/create-local-appointment-fixtures.sh

# Create ten independent fixtures
./scripts/create-local-appointment-fixtures.sh 10
```

The script first checks that every service in `docker-compose.yml` is running and that the local API responds on its health endpoint. It creates or updates `appointment.fixtures@digital.justice.gov.uk` with the HMPPS Auth role `IPB_FRONTEND_RW` (emitted in tokens as `ROLE_IPB_FRONTEND_RW`), its matching Seetec provider group, obtains a local HMPPS Auth token through the authorization-code flow, and invokes the endpoint. It provisions this fixed local user directly in the local Auth database. Set `ACCESS_TOKEN` to skip local user setup and token generation, or `API_BASE_URL` to target a different local API address. The endpoint uses the JPA entities and repositories to create a `person`, persisted `person_additional_details`, a submitted `referral`, and its `CREATED`/`SUBMITTED` events. It also registers a high-priority exact-CRN nDelius stub with the local WireMock Admin API.

The response contains the case reference, referral UUID, and CRN for every fixture. Use either the case reference or referral UUID in the endpoint path; for example:

```text
GET /bff/referral/AA1234BB/appointments
```

The live route is singular (`/bff/referral/...`), rather than `/bff/referrals/...`. The admin controller is loaded only by the `local` profile, requires `ROLE_IPB_FRONTEND_RW`, and is not present in deployed environments.



## OpenAPI contract checks

Pull requests targeting `main` run the OpenAPI contract check. The workflow starts the application using the existing integration-test setup, exports the OpenAPI document, and compares it with the committed baseline at `openapi/openapi-baseline.json`.

If the OpenAPI contract has changed, the `open-api-changed` check fails, the `ui-pr-required` label is applied to the GitHub PR, and a comment is added to the pull request.

Before merging, check whether the UI types need updating. The process for generating types is documented in the [Generating API Types](https://github.com/ministryofjustice/hmpps-community-support-ui#generating-api-types) section of the UI repository README.

When a corresponding UI pull request has been opened:

1. add a comment containing a link to that PR to your API PR
2. apply the `ui-pr-created` label to the API pull request.

The label workflow will then pass the `open-api-changed` check.

If you push another API change after claiming the UI work, the contract check deliberately fails again and asks you to double-check the UI types. Apply `ui-pr-created` again only after confirming that the latest API contract change has been handled by the UI.

### First-run bootstrap

The OpenAPI baseline is generated only on `main`; do not create or edit `openapi/openapi-baseline.json` by hand.

When setting this up for the first time:

1. Merge the workflow changes to `main`, using an administrator or ruleset bypass for the initial merge because the baseline and required check do not exist yet.
2. Run the `Update OpenAPI baseline` workflow manually against `main`.
3. The workflow generates and commits `openapi/openapi-baseline.json` to `main`.
4. Re-enable the GitHub Actions workflow for PRs targeting `main`.

After this bootstrap, the baseline updater runs automatically after pushes to `main`. The pull request workflow compares against that trusted baseline and feature branches must not edit it directly.
