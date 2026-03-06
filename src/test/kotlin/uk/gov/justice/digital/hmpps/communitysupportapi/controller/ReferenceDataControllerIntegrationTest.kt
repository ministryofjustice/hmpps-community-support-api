package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.model.ProbationOffice

class ReferenceDataControllerIntegrationTest : IntegrationTestBase() {

  @Nested
  @DisplayName("GET /bff/reference-data/probation-offices")
  inner class ReferenceDataEndpoint {

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(HttpMethod.GET, "/bff/reference-data/probation-offices")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(HttpMethod.GET, "/bff/reference-data/probation-offices")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(HttpMethod.GET, "/bff/reference-data/probation-offices")
    }

    @Test
    fun `should return list of probation offices information`() {
      val response = webTestClient.get()
        .uri { uriBuilder ->
          uriBuilder
            .path("/bff/reference-data/probation-offices")
            .build()
        }
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody(object : ParameterizedTypeReference<List<ProbationOffice>>() {})
        .returnResult().responseBody!!

      assertThat(response).hasSize(130)
      response.forEach { probationOffice ->
        assertThat(probationOffice.probationOfficeId).isNotNull()
        assertThat(probationOffice.name).isNotBlank()
        assertThat(probationOffice.address).isNotBlank()
        assertThat(probationOffice.probationRegionId).isNotBlank()
      }
    }
  }
}
