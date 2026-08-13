package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CommunitySupportServicesDto
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository

class CommunityServiceProviderIntegrationTest : IntegrationTestBase() {

  @MockitoSpyBean
  lateinit var communityServiceProviderRepository: CommunityServiceProviderRepository

  @Test
  fun `should return community service providers grouped by region`() {
    val response = webTestClient
      .method(GET)
      .uri("/bff/referral-select-a-service")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_IPB_FRONTEND_RW")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody(object : ParameterizedTypeReference<CommunitySupportServicesDto>() {})
      .returnResult().responseBody!!

    assertThat(response.communitySupportServices).isNotEmpty
    assertThat(response.communitySupportServices.values.flatten()).hasSize(27)

    assertThat(response.communitySupportServices.keys).contains("North East", "North West", "South Central")
    assertThat(response.communitySupportServices.keys).allMatch { it.isNotBlank() }

    assertThat(response.communitySupportServices.values.flatten()).allMatch { it.pdus.isNotEmpty() }
    assertThat(response.communitySupportServices.values.flatten()).allMatch { it.area.isNotBlank() }

    response.communitySupportServices.forEach { (region, providers) ->
      assertThat(providers).allMatch { it.region == region }
    }
  }

  @Test
  fun `should return 401 when no auth header`() {
    assertUnauthorized(GET, "/bff/referral-select-a-service")
  }

  @Test
  fun `should return 403 when user has no required role`() {
    assertForbiddenWrongRole(GET, "/bff/referral-select-a-service")
  }

  @Test
  fun `should return 500 when repository throws`() {
    doThrow(RuntimeException("error when calling community service provider data")).whenever(communityServiceProviderRepository).findAll()

    assertServerError(GET, "/bff/referral-select-a-service")
  }
}
