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
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.PersonCircumstanceDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createCommunityManager
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createHomeOfficeInterest
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createPersonDetailsAndCircumstances
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.personDetailsAndCircumstancesNotFoundJson
import java.time.OffsetDateTime
import java.time.ZoneOffset

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
            .withBody(createPersonDetailsAndCircumstances()),
        ),
    )

    val result = nDeliusClient.getPersonDetailsAndCircumstancesByCrn(CRN)

    assertThat(result).isNotNull
    assertThat(result.preferredLanguage?.code).isEqualTo("EN")
    assertThat(result.preferredLanguage?.description).isEqualTo("English")

    assertThat(result.personCircumstances.size).isEqualTo(3)
    checkPersonCircumstanceDto(
      result.personCircumstances[0],
      CodeDescriptionDto("REL", "Relationships"),
      CodeDescriptionDto("REL_SUB", "Relationships sub type"),
      OffsetDateTime.of(2026, 3, 12, 14, 25, 0, 0, ZoneOffset.ofHours(1)),
    )
    checkPersonCircumstanceDto(
      result.personCircumstances[1],
      CodeDescriptionDto("EMP", "Employment"),
      CodeDescriptionDto("EMP_SUB", "Employment sub type"),
      OffsetDateTime.of(2026, 2, 12, 14, 25, 0, 0, ZoneOffset.ofHours(1)),
    )
    checkPersonCircumstanceDto(
      result.personCircumstances[2],
      CodeDescriptionDto("DEP", "Dependants"),
      CodeDescriptionDto("DEP_SUB", "Dependants sub type"),
      OffsetDateTime.of(2026, 1, 12, 14, 25, 0, 0, ZoneOffset.ofHours(1)),
    )

    assertThat(result.disabilities.size).isEqualTo(1)
    checkDisabilitiesDto(result.disabilities[0], CodeDescriptionDto("BLN", "Blind"), OffsetDateTime.of(2026, 3, 12, 14, 25, 0, 0, ZoneOffset.ofHours(1)))

    assertThat(result.offenderPersonalityDisorder?.status?.code).isEqualTo("NO")
    assertThat(result.offenderPersonalityDisorder?.status?.description).isEqualTo("N/A")
  }

  @Test
  fun `person details should throw NotFoundException when nDelius API returns 404`() {
    stubFor(
      get(urlEqualTo("/case/UNKNOWN"))
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
      get(urlEqualTo("/case/UNKOWN/home-office-interest"))
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

  @Test
  fun `should return community manager when nDelius API returns 200`() {
    stubFor(
      get(urlEqualTo("/case/$CRN/community-manager"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(createCommunityManager()),
        ),
    )

    val result = nDeliusClient.getCommunityManagerByCrn(CRN)

    assertThat(result).isNotNull
    assertThat(result.crn).isEqualTo(CRN)
    assertThat(result.communityManager?.name?.forename).isEqualTo("TestForename")
    assertThat(result.communityManager?.name?.surname).isEqualTo("TestSurname")
    assertThat(result.communityManager?.emailAddress).isEqualTo("testForename.testSurname@digital.justice.gov.uk")
    assertThat(result.communityManager?.pdu).isEqualTo("Northumberland")
  }

  @Test
  fun `community manager should throw NotFoundException when nDelius API returns 404`() {
    stubFor(
      get(urlEqualTo("/case/UNKNOWN/community-manager"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json")
            .withBody(personDetailsAndCircumstancesNotFoundJson()),
        ),
    )

    assertThrows(NotFoundException::class.java) {
      nDeliusClient.getCommunityManagerByCrn("UNKNOWN")
    }
  }

  fun checkPersonCircumstanceDto(circumstance: PersonCircumstanceDto, expectedType: CodeDescriptionDto, expectedSubType: CodeDescriptionDto, expectedUpdatedAt: OffsetDateTime) {
    assertThat(circumstance.type?.code).isEqualTo(expectedType.code)
    assertThat(circumstance.type?.description).isEqualTo(expectedType.description)
    assertThat(circumstance.subtype?.code).isEqualTo(expectedSubType.code)
    assertThat(circumstance.subtype?.description).isEqualTo(expectedSubType.description)
    assertThat(circumstance.updatedAt).isEqualTo(expectedUpdatedAt)
  }

  fun checkDisabilitiesDto(disabilities: DisabilitiesDto, expectedType: CodeDescriptionDto, expectedUpdatedAt: OffsetDateTime) {
    assertThat(disabilities.type?.code).isEqualTo(expectedType.code)
    assertThat(disabilities.type?.description).isEqualTo(expectedType.description)
    assertThat(disabilities.updatedAt).isEqualTo(expectedUpdatedAt)
  }
}
