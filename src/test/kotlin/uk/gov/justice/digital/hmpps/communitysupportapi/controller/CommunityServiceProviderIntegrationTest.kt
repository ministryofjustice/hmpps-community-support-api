package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
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
  fun `should return community service providers`() {
    val response = webTestClient
      .method(HttpMethod.GET)
      .uri("/bff/referral-select-a-service?personDetailsId=1234567890123456")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_IPB_FRONTEND_RW")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody(object : ParameterizedTypeReference<CommunitySupportServicesDto>() {})
      .returnResult().responseBody!!

    assertThat(response.communitySupportServices).isNotEmpty
    assertThat(response.communitySupportServices.size).isEqualTo(27)

    val locationNames = response.communitySupportServices.map { it.region }.map { it.trim() }.toSet()

    assertThat(locationNames).isNotEmpty
    assertThat(locationNames).allMatch { it.isNotBlank() }

    assertThat(locationNames).contains("Cleveland", "Lancashire", "Thames Valley")
  }

  @Test
  fun `should return 401 when no auth header`() {
    webTestClient
      .method(HttpMethod.GET)
      .uri("/bff/referral-select-a-service?personDetailsId=1234567890123456")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return 403 when user has no required role`() {
    webTestClient
      .method(HttpMethod.GET)
      .uri("/bff/referral-select-a-service?personDetailsId=1234567890123456")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_SOME_OTHER")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return 500 when repository throws`() {
    doThrow(RuntimeException("error when calling community service provider data")).whenever(communityServiceProviderRepository).findAll()
    webTestClient
      .method(HttpMethod.GET)
      .uri("/bff/referral-select-a-service?personDetailsId=1234567890123456")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_IPB_FRONTEND_RW")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .is5xxServerError
  }
}
