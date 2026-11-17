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

## Pact contract testing

This service consumes the [hmpps-person-record](https://github.com/ministryofjustice/hmpps-person-record) API
(via `CorePersonRecordClient`) and verifies that integration using
[Pact](https://docs.pact.io/) consumer contract tests.

Pact tests live alongside other tests under `src/test/kotlin` and are named `*PactTest` (e.g.
`CorePersonRecordClientPactTest`). They are excluded from the normal `test` task and run via a dedicated task instead.

### Running Pact tests locally

```bash
./gradlew pactTest
```

This spins up a Pact mock provider, exercises `CorePersonRecordClient` against it, and writes the generated
contract to `build/pacts/hmpps-community-support-api-hmpps-person-record.json`. It does **not** publish the
contract to the Pact Broker.

### Writing a new Pact test

- Add a new `@Pact` interaction describing the request/response you expect from `hmpps-person-record`, matching
  an existing `@State` provider state defined in that repo (see its `*PactTest`/`AbstractApiProviderPactTests`
  classes) so the published contract can actually be verified by the provider.
- Only assert on the fields this service actually uses from the response - Pact does not require (or check)
  fields the provider returns that the consumer ignores.
- Keep the consumer name (`hmpps-community-support-api`) and provider name (`hmpps-person-record`) constants
  unchanged, as the Pact Broker uses these to associate contracts with verification results.

### CI pipeline

On every push, `pipeline.yml` runs `pact_test_publish.yml`, which executes `./gradlew pactTest` and then
`./gradlew pactPublish` to publish the generated contract(s) to the Pact Broker tagged with the branch name and
app version. It then calls `trigger_pact_provider_verification.yml`, which dispatches the
`pact_provider_verification.yml` workflow in `hmpps-person-record` to verify the newly published contract against
that provider.

Before deploying to an environment, `pact_deploy_env.yml` runs a Pact `can-i-deploy` check (via
`pact_consumer_deployment: true`) to confirm the version being deployed has a successful, up-to-date provider
verification, and records the deployment in the Pact Broker once it succeeds.

### Required GitHub secrets

The following secrets must be configured on this repository (or inherited from the `ministryofjustice` org) for
the Pact workflows to run successfully:

| Secret | Purpose |
| --- | --- |
| `PACT_BROKER_URL` | URL of the MoJ Pact Broker used to publish/verify contracts. |
| `HMPPS_PACT_BROKER_USERNAME` | Username for authenticating with the Pact Broker. |
| `HMPPS_PACT_BROKER_PASSWORD` | Password for authenticating with the Pact Broker. |
| `PACT_TRIGGER_APP_ID` | GitHub App ID used to generate a token for dispatching the provider verification workflow in `hmpps-person-record`. The app must be installed on `hmpps-person-record` with `actions: write` permission. |
| `PACT_TRIGGER_PRIVATE_KEY` | Private key for the same GitHub App. |

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

