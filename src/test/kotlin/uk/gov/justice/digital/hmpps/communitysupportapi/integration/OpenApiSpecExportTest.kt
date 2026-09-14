package uk.gov.justice.digital.hmpps.communitysupportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.nio.file.Files
import java.nio.file.Path

/**
 * Exports the current OpenAPI spec so CI can diff it against the committed baseline
 * (see openapi/openapi-baseline.json and .github/workflows/openapi_contract_check.yml)
 * to detect breaking API changes without needing a separately built/deployed app.
 */
class OpenApiSpecExportTest : IntegrationTestBase() {

  @Test
  fun `export the open api spec for contract diffing`() {
    val body = webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .returnResult()
      .responseBody!!

    val outputPath = Path.of("build/openapi/openapi.json")
    Files.createDirectories(outputPath.parent)
    Files.writeString(outputPath, body)
  }
}
