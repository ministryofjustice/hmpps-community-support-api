package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.GET
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CommunitySupportRiskInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.arns.CommunitySupportRiskDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.RiskInformationRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.arnsRoshRiskJson
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.arnsRoshRiskNotFoundJson
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.arnsStaleRoshRiskJson
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.RiskInformationFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedAssessmentDate
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedDateOfBirthLong
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class RiskControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var riskInformationRepository: RiskInformationRepository

  @MockitoBean
  private lateinit var userMapper: UserMapper

  private lateinit var testUser: ReferralUser

  @Nested
  @DisplayName("GET /bff/risk/rosh/{referralId}")
  inner class RoshRisksEndpoint {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(GET, "/bff/risk/rosh/${UUID.randomUUID()}")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(GET, "/bff/risk/rosh/${UUID.randomUUID()}")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(GET, "/bff/risk/rosh/${UUID.randomUUID()}")
    }

    @Test
    fun `should return Not Found when referral does not exist`() {
      assertNotFound(GET, "/bff/risk/rosh/${UUID.randomUUID()}")
    }

    @Test
    fun `should return full risk data when assessment is within 12 months`() {
      val referralUser = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson()
      val referral = referralHelper.createDraftReferral(person = person, createdBy = referralUser.id)

      val assessedOn = LocalDateTime.now().minusDays(30)
      stubFor(
        get(urlEqualTo("/risks/crn/$CRN"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(arnsRoshRiskJson(assessedOn)),
          ),
      )

      webTestClient.get()
        .uri("/bff/risk/rosh/${referral.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<CommunitySupportRiskDto>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.firstName shouldBe "John"
          body.lastName shouldBe "Smith"
          body.crn shouldBe CRN
          body.dateOfBirth shouldBe LocalDate.of(1980, 1, 1).toFormattedDateOfBirthLong()
          body.assessmentWithin12Months shouldBe true
          body.assessedOn shouldBe assessedOn.toFormattedAssessmentDate()
          body.summary shouldNotBe null
          body.summary?.overallRiskLevel shouldBe "HIGH"
          body.summary?.whoIsAtRisk shouldBe "Staff and public are at risk"
          body.summary?.riskInCommunity?.get("HIGH") shouldBe listOf("Public", "Known Adult")
          body.summary?.riskInCustody?.get("VERY_HIGH") shouldBe listOf("Staff")
          body.riskToSelf shouldNotBe null
          body.riskToSelf?.suicide?.riskIndicator shouldBe "YES"
          body.riskToSelf?.suicide?.currentConcernsReason shouldBe "Current suicide concerns"
          body.riskToSelf?.vulnerability?.currentConcernsReason shouldBe "Vulnerability concerns noted"
        }
    }

    @Test
    fun `should return blank response with assessmentWithin12Months false when assessment is older than 12 months`() {
      val referralUser = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson()
      val referral = referralHelper.createDraftReferral(person = person, createdBy = referralUser.id)

      stubFor(
        get(urlEqualTo("/risks/crn/$CRN"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(arnsStaleRoshRiskJson()),
          ),
      )

      webTestClient.get()
        .uri("/bff/risk/rosh/${referral.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<CommunitySupportRiskDto>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.firstName shouldBe "John"
          body.lastName shouldBe "Smith"
          body.crn shouldBe CRN
          body.assessmentWithin12Months shouldBe false
          body.assessedOn shouldBe null
          body.riskToSelf shouldBe null
          body.summary shouldBe null
        }
    }

    @Test
    fun `should return Not Found when CRN is not found in Assess Risks and Needs`() {
      val referralUser = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson()
      val referral = referralHelper.createDraftReferral(person = person, createdBy = referralUser.id)

      stubFor(
        get(urlEqualTo("/risks/crn/$CRN"))
          .willReturn(
            aResponse()
              .withStatus(404)
              .withHeader("Content-Type", "application/json")
              .withBody(arnsRoshRiskNotFoundJson()),
          ),
      )

      assertNotFound(GET, "/bff/risk/rosh/${referral.id}")
    }

    @Test
    fun `should return 500 when ARNS service is unavailable`() {
      val referralUser = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson()
      val referral = referralHelper.createDraftReferral(person = person, createdBy = referralUser.id)

      stubFor(
        get(urlEqualTo("/risks/crn/$CRN"))
          .willReturn(
            aResponse()
              .withStatus(500),
          ),
      )

      assertServerError(GET, "/bff/risk/rosh/${referral.id}")
    }
  }

  @Nested
  @DisplayName("PUT /risk-information/{referralId}")
  inner class SaveRiskInformationEndpoint {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(HttpMethod.PUT, "/risk-information/${UUID.randomUUID()}")
    }

    @Test
    fun `should return forbidden if no role`() {
      val request = CommunitySupportRiskInformationDto(id = UUID.randomUUID(), referralId = UUID.randomUUID())
      assertForbiddenNoRole(HttpMethod.PUT, "/risk-information/${UUID.randomUUID()}", request)
    }

    @Test
    fun `should return forbidden if wrong role`() {
      val request = CommunitySupportRiskInformationDto(id = UUID.randomUUID(), referralId = UUID.randomUUID())
      assertForbiddenWrongRole(HttpMethod.PUT, "/risk-information/${UUID.randomUUID()}", request)
    }

    @Test
    fun `should return 404 when referral does not exist`() {
      val request = CommunitySupportRiskInformationDto(
        id = UUID.randomUUID(),
        referralId = UUID.randomUUID(),
        riskSummaryWhoIsAtRisk = "Staff and public",
      )

      webTestClient.put()
        .uri("/risk-information/${UUID.randomUUID()}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `should create draft risk information when none exists for the referral`() {
      val person = referralHelper.createPerson()
      val referral = referralHelper.createDraftReferral(person = person, createdBy = testUser.id)

      val request = CommunitySupportRiskInformationDto(
        id = UUID.randomUUID(),
        referralId = referral.id,
        riskSummaryWhoIsAtRisk = "Staff and public are at risk",
        riskSummaryNatureOfRisk = "Physical harm",
        riskSummaryRiskImminence = "Low",
        riskToSelfSuicide = "No current concerns",
        riskToSelfSelfHarm = "No current concerns",
        riskToSelfHostelSetting = "Not applicable",
        riskToSelfVulnerability = "None identified",
        additionalInformation = "Some additional notes",
      )

      webTestClient.put()
        .uri("/risk-information/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<CommunitySupportRiskInformationDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.referralId shouldBe referral.id
          body.riskSummaryWhoIsAtRisk shouldBe "Staff and public are at risk"
          body.riskSummaryNatureOfRisk shouldBe "Physical harm"
          body.riskSummaryRiskImminence shouldBe "Low"
          body.riskToSelfSuicide shouldBe "No current concerns"
          body.riskToSelfSelfHarm shouldBe "No current concerns"
          body.riskToSelfHostelSetting shouldBe "Not applicable"
          body.riskToSelfVulnerability shouldBe "None identified"
          body.additionalInformation shouldBe "Some additional notes"
        }

      val saved = riskInformationRepository.findByReferralId(referral.id)
      saved shouldNotBe null
      saved!!.updatedBy shouldBe testUser.id
      saved.riskSummaryWhoIsAtRisk shouldBe "Staff and public are at risk"
    }

    @Test
    fun `should update existing draft risk information for the referral`() {
      val person = referralHelper.createPerson()
      val referral = referralHelper.createDraftReferral(person = person, createdBy = testUser.id)

      val existing = RiskInformationFactory()
        .withReferral(referral)
        .withRiskSummaryWhoIsAtRisk("Old summary")
        .withUpdatedBy(testUser.id)
        .create()
      riskInformationRepository.save(existing)

      val request = CommunitySupportRiskInformationDto(
        id = UUID.randomUUID(),
        referralId = referral.id,
        riskSummaryWhoIsAtRisk = "Updated summary",
        riskToSelfVulnerability = "Vulnerability identified",
      )

      webTestClient.put()
        .uri("/risk-information/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<CommunitySupportRiskInformationDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.id shouldBe existing.id
          body.riskSummaryWhoIsAtRisk shouldBe "Updated summary"
          body.riskToSelfVulnerability shouldBe "Vulnerability identified"
        }

      val updated = riskInformationRepository.findByReferralId(referral.id)!!
      updated.id shouldBe existing.id
      updated.updatedBy shouldBe testUser.id
      updated.riskSummaryWhoIsAtRisk shouldBe "Updated summary"
      updated.riskToSelfVulnerability shouldBe "Vulnerability identified"
    }
  }
}
