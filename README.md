# hmpps-community-support-api

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/hmpps-community-support-api/badge?style=flat)](https://github-community.service.justice.gov.uk/repository-standards/hmpps-community-support-api)
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-community-support-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://community-support-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)

Template github repo used for new Kotlin based projects.

# Instructions

If this is a HMPPS project then the project will be created as part of bootstrapping -
see [hmpps-project-bootstrap](https://github.com/ministryofjustice/hmpps-project-bootstrap). You are able to specify a
template application using the `github_template_repo` attribute to clone without the need to manually do this yourself
within GitHub.

This project is community managed by the mojdt `#kotlin-dev` slack channel.
Please raise any questions or queries there. Contributions welcome!

Our security policy is located [here](https://github.com/ministryofjustice/hmpps-community-support-api/security/policy).

Documentation to create new service is located [here](https://tech-docs.hmpps.service.justice.gov.uk/creating-new-services/).

## Creating a Cloud Platform namespace

When deploying to a new namespace, you may wish to use the
[templates project namespace](https://github.com/ministryofjustice/cloud-platform-environments/tree/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-templates-dev)
as the basis for your new namespace. This namespace contains both the kotlin and typescript template projects,
which is the usual way that projects are setup.

Copy this folder and update all the existing namespace references to correspond to the environment to which you're deploying.

If you only need the kotlin configuration then remove all typescript references and remove the elasticache configuration.

To ensure the correct github teams can approve releases, you will need to make changes to the configuration in `resources/service-account-github` where the appropriate team names will need to be added (based on [lines 98-100](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-templates-dev/resources/serviceaccount-github.tf#L98) and the reference appended to the teams list below [line 112](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-templates-dev/resources/serviceaccount-github.tf#L112)). Note: hmpps-sre is in this list to assist with deployment issues.

Submit a PR to the Cloud Platform team in
# ask-cloud-platform. Further instructions from the Cloud Platform team can be found in
the [Cloud Platform User Guide](https://user-guide.cloud-platform.service.justice.gov.uk/#cloud-platform-user-guide)

## Renaming from HMPPS Community Support Api - github Actions

Once the new repository is deployed. Navigate to the repository in github, and select the `Actions` tab.
Click the link to `Enable Actions on this repository`.

Find the Action workflow named: `rename-project-create-pr` and click `Run workflow`. This workflow will
execute the `rename-project.bash` and create Pull Request for you to review. Review the PR and merge.

Note: ideally this workflow would run automatically however due to a recent change github Actions are not
enabled by default on newly created repos. There is no way to enable Actions other then to click the button in the UI.
If this situation changes we will update this project so that the workflow is triggered during the bootstrap project.
Further reading: <https://github.community/t/workflow-isnt-enabled-in-repos-generated-from-template/136421>

The script takes six arguments:

### New project name

This should start with `hmpps-` e.g. `hmpps-prison-visits` so that it can be easily distinguished in github from
other departments projects. Try to avoid using abbreviations so that others can understand easily what your project is.

### Slack channel for release notifications

By default, release notifications are only enabled for production. The circleci configuration can be amended to send
release notifications for deployments to other environments if required. Note that if the configuration is amended,
the slack channel should then be amended to your own team's channel as `dps-releases` is strictly for production release
notifications. If the slack channel is set to something other than `dps-releases`, production release notifications
will still automatically go to `dps-releases` as well. This is configured by `releases-slack-channel` in
`.circleci/config.yml`.

### Slack channel for pipeline security notifications

Ths channel should be specific to your team and is for daily / weekly security scanning job results. It is your team's
responsibility to keep up-to-date with security issues and update your application so that these jobs pass. You will
only be notified if the jobs fail. The scan results can always be found in GitHub actions and results are sent to the GitHub security tab. This is
configured by setting GitHub actions environment variable called `SECURITY_ALERTS_SLACK_CHANNEL_ID`.

### Non production kubernetes alerts

By default Prometheus alerts are created in the application namespaces to monitor your application e.g. if your
application is crash looping, there are a significant number of errors from the ingress. Since Prometheus runs in
cloud platform AlertManager needs to be setup first with your channel. Please see
[Create your own custom alerts](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/monitoring-an-app/how-to-create-alarms.html)
in the Cloud Platform user guide. Once that is setup then the `custom severity label` can be used for
`alertSeverity` in the `helm_deploy/values-*.yaml` configuration.

Normally it is worth setting up two separate labels and therefore two separate slack channels - one for your production
alerts and one for your non-production alerts. Using the same channel can mean that production alerts are sometimes
lost within non-production issues.

### Production kubernetes alerts

This is the severity label for production, determined by the `custom severity label`. See the above
# non-production-kubernetes-alerts for more information. This is configured in `helm_deploy/values-prod.yaml`.

### Product ID

This is so that we can link a component to a product and thus provide team and product information in the Developer
Portal. Refer to the developer portal at <https://developer-portal.hmpps.service.justice.gov.uk/products> to find your
product id. This is configured in `helm_deploy/<project_name>/values.yaml`.

## Manually branding from template app

Run the `rename-project.bash` without any arguments. This will prompt for the six required parameters and create a PR.
The script requires a recent version of `bash` to be installed, as well as GNU `sed` in the path.

## Common Kotlin patterns

Many patterns have evolved for HMPPS Kotlin applications. Using these patterns provides consistency across our suite of
Kotlin microservices and allows you to concentrate on building your business needs rather than reinventing the
technical approach.

Documentation for these patterns can be found in the [HMPPS tech docs](https://tech-docs.hmpps.service.justice.gov.uk/common-kotlin-patterns/).
If this documentation is incorrect or needs improving please report to [#ask-prisons-digital-sre](https://moj.enterprise.slack.com/archives/C06MWP0UKDE)
or [raise a PR](https://github.com/ministryofjustice/hmpps-tech-docs).

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

It's safe safe to re-run this script (i.e. it won't re-create identical users). 

## Linting

To lint run the following
`./gradlew ktlintCheck`

Or to automatically format
`./gradlew ktlintFormat`

## Testing

Run
`./gradlew clean test`

## Building

`./gradlew clean build`
