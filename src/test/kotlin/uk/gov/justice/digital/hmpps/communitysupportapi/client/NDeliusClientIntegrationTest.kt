package uk.gov.justice.digital.hmpps.communitysupportapi.client

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CodeDescriptionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.DisabilitiesDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.PersonalCircumstanceDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createHomeOfficeInterest
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createPersonalDetailsAndCircumstances
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.personDetailsAndCircumstancesNotFoundJson
import java.time.LocalDateTime

class NDeliusClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var nDeliusClient: NDeliusClient

  @Test
  fun `should return person details and circumstances when nDelius API returns 200`() {
    stubFor(
      get(urlEqualTo("/case/$CRN"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(createPersonalDetailsAndCircumstances()),
        ),
    )

    val result = nDeliusClient.getPersonDetailsAndCircumstancesByCrn(CRN)

    assertThat(result).isNotNull
    assertThat(result.preferredLanguage?.code).isEqualTo("EN")
    assertThat(result.preferredLanguage?.description).isEqualTo("English")

    assertThat(result.personalCircumstances.size).isEqualTo(3)
    checkPersonalCircumstanceDto(
      result.personalCircumstances[0],
      CodeDescriptionDto("REL", "Relationships"),
      CodeDescriptionDto("REL_SUB", "Relationships sub type"),
      LocalDateTime.of(2026, 3, 12, 14, 25, 0),
    )
    checkPersonalCircumstanceDto(
      result.personalCircumstances[1],
      CodeDescriptionDto("EMP", "Employment"),
      CodeDescriptionDto("EMP_SUB", "Employment sub type"),
      LocalDateTime.of(2026, 2, 12, 14, 25, 0),
    )
    checkPersonalCircumstanceDto(
      result.personalCircumstances[2],
      CodeDescriptionDto("DEP", "Dependants"),
      CodeDescriptionDto("DEP_SUB", "Dependants sub type"),
      LocalDateTime.of(2026, 1, 12, 14, 25, 0),
    )

    assertThat(result.disabilities.size).isEqualTo(1)
    checkDisabilitiesDto(result.disabilities[0], CodeDescriptionDto("BLN", "Blind"), LocalDateTime.of(2026, 3, 12, 14, 25, 0))

    assertThat(result.offenderPersonalityDisorder?.status?.code).isEqualTo("NO")
    assertThat(result.offenderPersonalityDisorder?.status?.description).isEqualTo("N/A")
  }

  @Test
  fun `person details should throw NotFoundException when nDelius API returns 404`() {
    stubFor(
      get(urlEqualTo("/case/UKNOWN"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json")
            .withBody(personDetailsAndCircumstancesNotFoundJson()),
        ),
    )

    assertThrows(NotFoundException::class.java) {
      nDeliusClient.getPersonDetailsAndCircumstancesByCrn("UNKNOWN")
    }
  }

  @Test
  fun `should return home office interest when nDelius API returns 200`() {
    stubFor(
      get(urlEqualTo("/case/$CRN/home-office-interest"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(createHomeOfficeInterest()),
        ),
    )

    val result = nDeliusClient.getHomeOfficeInterestByCrn(CRN)

    assertThat(result).isNotNull
    assertThat(result.exists).isTrue()
    assertThat(result.notes).isEqualTo("Is of interest")
  }

  @Test
  fun `home office interest should throw NotFoundException when nDelius API returns 404`() {
    stubFor(
      get(urlEqualTo("/case/UKNOWN/home-office-interest"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json")
            .withBody(personDetailsAndCircumstancesNotFoundJson()),
        ),
    )

    assertThrows(NotFoundException::class.java) {
      nDeliusClient.getHomeOfficeInterestByCrn("UNKNOWN")
    }
  }

  fun checkPersonalCircumstanceDto(circumstance: PersonalCircumstanceDto, expectedType: CodeDescriptionDto, expectedSubType: CodeDescriptionDto, expectedUpdatedAt: LocalDateTime) {
    assertThat(circumstance.type?.code).isEqualTo(expectedType.code)
    assertThat(circumstance.type?.description).isEqualTo(expectedType.description)
    assertThat(circumstance.subtype?.code).isEqualTo(expectedSubType.code)
    assertThat(circumstance.subtype?.description).isEqualTo(expectedSubType.description)
    assertThat(circumstance.updatedAt).isEqualTo(expectedUpdatedAt)
  }

  fun checkDisabilitiesDto(disabilities: DisabilitiesDto, expectedType: CodeDescriptionDto, expectedUpdatedAt: LocalDateTime) {
    assertThat(disabilities.type?.code).isEqualTo(expectedType.code)
    assertThat(disabilities.type?.description).isEqualTo(expectedType.description)
    assertThat(disabilities.updatedAt).isEqualTo(expectedUpdatedAt)
  }
}
