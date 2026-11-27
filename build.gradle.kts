import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "11.1.2"
  kotlin("plugin.spring") version "2.4.21"
  kotlin("plugin.jpa") version "2.4.21"
  id("au.com.dius.pact") version "4.7.7"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

// Pact broker connection details are supplied via environment variables so that the pipeline can
// inject the real credentials while local runs fall back to sensible defaults.
pact {
  publish {
    pactDirectory = layout.buildDirectory.dir("pacts").get().asFile
    pactBrokerUrl = System.getenv("PACT_BROKER_URL") ?: "https://pact-broker-prod.apps.live-1.cloud-platform.service.justice.gov.uk"
    pactBrokerUsername = System.getenv("HMPPS_PACT_BROKER_USERNAME")
    pactBrokerPassword = System.getenv("HMPPS_PACT_BROKER_PASSWORD")
    consumerVersion = System.getenv("APP_VERSION") ?: "local"
    consumerBranch = System.getenv("GITHUB_BRANCH") ?: "local"
  }
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:3.1.0")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  // Springdoc 3.2.1 moved to swagger-core 3.0.0 (Jackson 3), while the latest swagger-parser 2.1.49
  // still depends on swagger-core 2.x and Jackson 2. Temporarily pinning Springdoc to 3.2.0
  // to avoid runtime issues with swagger-parser until it is updated to support swagger-core 3.0.0 and Jackson 3.
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0")
  // Swagger UI 5.35.0 bundles DOMPurify patched for CVE-2026-116802 and CVE-2026-116803.
  implementation("org.webjars:swagger-ui:5.36.0")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.springframework:spring-web")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("uk.gov.service.notify:notifications-java-client:6.2.1-RELEASE")

  // security
  implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
  implementation("org.springframework.security:spring-security-crypto:7.2.0")
  implementation("com.nimbusds:oauth2-oidc-sdk:11.39")

  // csv
  implementation("org.apache.commons:commons-csv:1.14.1")

  testImplementation("org.springframework.boot:spring-boot-starter-web")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:3.1.0")
  testImplementation("org.mockito.kotlin:mockito-kotlin:6.4.0")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.49") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("org.testcontainers:postgresql:1.21.4")
  testImplementation("org.testcontainers:junit-jupiter:1.21.4")
  testImplementation("org.awaitility:awaitility-kotlin")
  testImplementation("com.ninja-squad:springmockk:5.0.1")
  testImplementation("io.kotest:kotest-assertions-core:6.2.7")
  testImplementation("io.mockk:mockk:1.14.12")
  testImplementation("au.com.dius.pact.consumer:junit5:4.7.7")

  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.15")

  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")
}

kotlin {
  jvmToolchain(25)
}

java {
  sourceCompatibility = JavaVersion.VERSION_24
  targetCompatibility = JavaVersion.VERSION_24
}

tasks {
  withType<KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24
  }
}

val compileKotlin = tasks.getByName<KotlinCompile>("compileKotlin")
compileKotlin.compilerOptions {
  freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
}

allOpen {
  annotation("jakarta.persistence.Entity")
  annotation("jakarta.persistence.MappedSuperclass")
  annotation("jakarta.persistence.Embeddable")
}
val compileTestKotlin = tasks.getByName<KotlinCompile>("compileTestKotlin")
compileTestKotlin.compilerOptions {
  freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
}

// This test is required for schema spy implementation and should NOT be run alongside our normal testsuite as it runs against a local application rather than the testcontainers instances.
// Pact consumer tests are also excluded from the normal test suite and run separately via the pactTest task below.
tasks.test {
  exclude("**/InitialiseDatabase.class")
  filter {
    excludeTestsMatching("*PactTest")
  }
}

tasks.register<Test>("pactTest") {
  group = "verification"
  description = "Runs consumer-side Pact tests"
  testClassesDirs = sourceSets.test.get().output.classesDirs
  classpath = sourceSets.test.get().runtimeClasspath
  useJUnitPlatform()
  filter {
    includeTestsMatching("*PactTest")
    isFailOnNoMatchingTests = false
  }
}

val test = testing.suites.named<JvmTestSuite>("test")

tasks.register<Test>("initialiseDatabase") {
  testClassesDirs = files(test.map { it.sources.output.classesDirs })
  classpath = files(test.map { it.sources.runtimeClasspath })
  include("**/InitialiseDatabase.class")
  onlyIf { gradle.startParameter.taskNames.contains("initialiseDatabase") }
}

tasks.register<JavaExec>("bootRunLocal") {
  group = "application"
  description = "Runs the Spring Boot application with the local profile."
  classpath = sourceSets.main.get().runtimeClasspath
  mainClass.set("uk.gov.justice.digital.hmpps.communitysupportapi.CommunitySupportApiKt")
  args = listOf("--spring.profiles.active=local")
}
