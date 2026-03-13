FROM --platform=${BUILDPLATFORM:-linux/amd64} ghcr.io/ministryofjustice/hmpps-eclipse-temurin:25-jre-jammy AS builder

ARG BUILD_NUMBER
ENV BUILD_NUMBER=${BUILD_NUMBER:-1_0_0}

WORKDIR /app
USER root
ENV GRADLE_USER_HOME=/tmp/gradle
RUN mkdir -p "$GRADLE_USER_HOME" && chmod -R 755 "$GRADLE_USER_HOME"

COPY . .
RUN ./gradlew bootJar --no-daemon

FROM ghcr.io/ministryofjustice/hmpps-eclipse-temurin:25-jre-jammy
LABEL maintainer="HMPPS Digital Studio <info@digital.justice.gov.uk>"

ARG BUILD_NUMBER
ENV BUILD_NUMBER=${BUILD_NUMBER:-1_0_0}

USER root
RUN apt-get update && \
    apt-get -y upgrade && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

ENV TZ=Europe/London
RUN ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone


# Install AWS RDS Root cert into Java truststore
RUN mkdir -p /home/appuser/.postgresql \
  && curl https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem \
    > /home/appuser/.postgresql/root.crt

WORKDIR /app
COPY --from=builder --chown=appuser:appgroup /app/build/libs/hmpps-community-support-api*.jar /app/app.jar
COPY --from=builder --chown=appuser:appgroup /app/build/libs/applicationinsights-agent*.jar /app/agent.jar
COPY --chown=appuser:appgroup applicationinsights.json /app
COPY --chown=appuser:appgroup applicationinsights.dev.json /app

USER 2000

ENTRYPOINT ["java", "-XX:+AlwaysActAsServerClassMachine", "-javaagent:/app/agent.jar", "-jar", "/app/app.jar"]
