package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CommunitySupportServiceDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PageResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository

class CommunityServiceProviderIntegrationTest : IntegrationTestBase() {

  @MockitoSpyBean
  lateinit var communityServiceProviderRepository: CommunityServiceProviderRepository

  @Test
  fun `should return community service providers paginated`() {
    val response = webTestClient
      .method(GET)
      .uri("/bff/referral-select-a-service")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_IPB_FRONTEND_RW")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody(object : ParameterizedTypeReference<PageResponse<CommunitySupportServiceDto>>() {})
      .returnResult().responseBody!!

    assertThat(response.content).hasSize(10)
    assertThat(response.page).isEqualTo(0)
    assertThat(response.size).isEqualTo(10)
    assertThat(response.totalElements).isEqualTo(27)
    assertThat(response.totalPages).isEqualTo(3)

    assertThat(response.content).allMatch { it.pdus.isNotEmpty() }
  }

  @Test
  fun `should return second page of community service providers`() {
    val response = webTestClient
      .method(GET)
      .uri("/bff/referral-select-a-service?page=1&size=10")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_IPB_FRONTEND_RW")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody(object : ParameterizedTypeReference<PageResponse<CommunitySupportServiceDto>>() {})
      .returnResult().responseBody!!

    assertThat(response.content).hasSize(10)
    assertThat(response.page).isEqualTo(1)
    assertThat(response.totalElements).isEqualTo(27)
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
    doThrow(RuntimeException("error when calling community service provider data")).whenever(communityServiceProviderRepository).findAll(any<Pageable>())

    assertServerError(GET, "/bff/referral-select-a-service")
  }
}
