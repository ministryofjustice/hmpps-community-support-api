# hmpps-community-support-api

# Quickstart

## Requirements

- Docker
- Java
- Postgres

## Running the application locally

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

### Creating local test users with custom roles

Once the stack is up, `scripts/local-user-setup.sh` creates a fully working local hmpps-auth
user (password set, no email step required) with whichever roles you need, entirely offline
(no GOV.UK Notify/real Delius/Nomis dependency):

```bash
./scripts/local-user-setup.sh [email] [ROLE1,ROLE2,...]
# defaults: local.tester@digital.justice.gov.uk / password123456 /
#           COMMUNITY_SUPPORT_REFERRER,COMMUNITY_SUPPORT_PROVIDER
```

It's safe to re-run this script (i.e. it won't re-create identical users). 

## Linting

To lint run the following
`./gradlew ktlintCheck`

Or to automatically format
`./gradlew ktlintFormat`

## Testing

Run
`./gradlew clean test`

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

## Building

`./gradlew clean build`

## Connecting to local database

If you want to connect to your local interventions service database, first exec onto the postgres container with:
```
docker exec -it ${containerId} /bin/sh
```
and then run:
```
psql -h localhost -d interventions -U postgres
```
or

we can use pgAdmin to connect to the database using the following connection details:
- Host: localhost
- Port: 5432
- Database: postgres
- Username: admin
- Password: password

## Seeding the local database

If you want to populate your local database with seeded values from [local data setup](/src/main/resources/db/local) then run:
```
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

## Migrating the local database

If the `community-support-api` DB on `localhost` isn't fully migrated, run either:
- `./gradlew bootRun` or run it with local profile in IDE (the service database is migrated on startup)
- `FLYWAY_POSTGRESQL_TRANSACTIONAL_LOCK=false flyway migrate -url=jdbc:postgresql://localhost:5432/interventions -user=postgres -password=password -locations=src/main/resources/db/migration` from the service directory

