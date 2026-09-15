package uk.gov.justice.digital.hmpps.communitysupportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.nio.file.Files
import java.nio.file.Path

/**
 * Exports the current OpenAPI spec so CI can diff it against the committed baseline.
 * Exports to: openapi/openapi-baseline.json
 * 
 * See openapi-baseline-update.yml and.openapi-contract-check.yml in the .github/workflows 
 * directory for specifics on generating and checking this file.
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
