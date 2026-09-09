package uk.gov.justice.digital.hmpps.communitysupportapi.service

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SelectionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ProbationPractitionerDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AdditionalSupportNeedsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CommunityServiceProviderRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.NeedsInterpreterRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.Pdu
import uk.gov.justice.digital.hmpps.communitysupportapi.model.UpdateProbationPractitionerDetailsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonAdditionalSupportNeedsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ProbationPractitionerDetailsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createCprProbationPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toJson
import java.time.OffsetDateTime
import java.util.*

private val COUNTY_DURHAM_AND_DARLINGTON_PDU_ID = UUID.fromString("63805267-d75e-485c-b8cd-ce15d63b6e7c")

class DraftReferralServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var draftReferralService: DraftReferralService

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var personAdditionSupportNeedsRepository: PersonAdditionalSupportNeedsRepository

  @Autowired
  private lateinit var communityServiceProviderRepository: CommunityServiceProviderRepository

  @Autowired
  private lateinit var referralProviderAssignmentRepository: ReferralProviderAssignmentRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var probationPractitionerDetailsRepository: ProbationPractitionerDetailsRepository

  @Test
  fun `update additional information should be saved`() {
    val referralUser = referralHelper.ensureReferralUser()
    val createReferralRequest = setUpData()

    val result = referralService.createReferral(referralUser.id, createReferralRequest)
    val savedReferral = result.referral

    val supportNeeds = AdditionalSupportNeedsRequest(
      employmentResponsibilities = "Test employment responsibilities",
      caringResponsibilities = "Test caring responsibilities",
      needsAdditionalSupport = true,
    )

    val updatedResult = draftReferralService.upsertAdditionalSupportNeeds(
      savedReferral.id,
      referralUser.id,
      supportNeeds,
    )
    assertThat(updatedResult).isNotNull()

    val savedSupportNeeds = personAdditionSupportNeedsRepository.findByReferralId(savedReferral.id)
    assertThat(savedSupportNeeds).isNotNull()
    assertThat(savedSupportNeeds?.referralId).isEqualTo(savedReferral.id)
    assertThat(savedSupportNeeds?.personId).isEqualTo(savedReferral.personId)
    assertThat(savedSupportNeeds?.caringResponsibilitiesDetails).isEqualTo("Test caring responsibilities")
    assertThat(savedSupportNeeds?.additionalSupportNeeded).isTrue()
    assertThat(savedSupportNeeds?.physicalHealthDetails).isNull()
    assertThat(savedSupportNeeds?.mentalEmotionalHealthDetails).isNull()
    assertThat(savedSupportNeeds?.diversityDetails).isNull()
    assertThat(savedSupportNeeds?.employmentResponsibilitiesDetails).isEqualTo("Test employment responsibilities")
    assertThat(savedSupportNeeds?.locationTravelDetails).isNull()
    assertThat(savedSupportNeeds?.neurodiversityDetails).isNull()
    assertThat(savedSupportNeeds?.anythingElseDetails).isNull()
    assertThat(savedSupportNeeds?.interpreterNeeded).isNull()
    assertThat(savedSupportNeeds?.interpreterLanguage).isNull()
    assertThat(savedSupportNeeds?.createdBy).isEqualTo(referralUser.id)
  }

  @Test
  fun `update interpreter needs should be saved`() {
    val referralUser = referralHelper.ensureReferralUser()
    val createReferralRequest = setUpData()

    val result = referralService.createReferral(referralUser.id, createReferralRequest)
    val savedReferral = result.referral

    val interpreterNeeds = NeedsInterpreterRequest(
      needsInterpreter = true,
      language = "Spanish",
    )

    val updatedResult = draftReferralService.upsertNeedsInterpreter(
      savedReferral.id,
      referralUser.id,
      interpreterNeeds,
    )
    assertThat(updatedResult).isNotNull()

    val savedInterpreterNeeds = personAdditionSupportNeedsRepository.findByReferralId(savedReferral.id)
    assertThat(savedInterpreterNeeds).isNotNull()
    assertThat(savedInterpreterNeeds?.referralId).isEqualTo(savedReferral.id)
    assertThat(savedInterpreterNeeds?.personId).isEqualTo(savedReferral.personId)
    assertThat(savedInterpreterNeeds?.interpreterLanguage).isEqualTo("Spanish")
    assertThat(savedInterpreterNeeds?.interpreterNeeded).isTrue()
    assertThat(savedInterpreterNeeds?.createdBy).isEqualTo(referralUser.id)
  }

    @Test
  fun `update community service provider should be saved`() {
    val referralUser = referralHelper.ensureReferralUser()
    val createReferralRequest = setUpData()
    val communityServiceProvider = referralHelper.getCommunityServiceProvider()

    val result = referralService.createReferral(referralUser.id, createReferralRequest)
    val savedReferral = result.referral

    val newCommunityServiceProvider = communityServiceProviderRepository.findAll()
      .first { it.id != communityServiceProvider.id }

    val request = CommunityServiceProviderRequest(
      communityServiceProviderId = newCommunityServiceProvider.id,
    )

    val updatedResult = draftReferralService.upsertCommunityServiceProvider(savedReferral.id, request)
    assertThat(updatedResult).isNotNull()
    assertThat(updatedResult.communityServiceProviderId).isEqualTo(newCommunityServiceProvider.id)
    assertThat(updatedResult.communityServiceProviderName).isEqualTo(newCommunityServiceProvider.name)

    val assignments = referralProviderAssignmentRepository.findByReferralId(savedReferral.id)
    assertThat(assignments).hasSize(1)
    assertThat(assignments.first().communityServiceProvider.id).isEqualTo(newCommunityServiceProvider.id)
  }

  @Test
  fun `update community service provider should throw not found for unknown referral`() {
    val unknownCommunityServiceProvider = referralHelper.getCommunityServiceProvider()

    val request = CommunityServiceProviderRequest(
      communityServiceProviderId = unknownCommunityServiceProvider.id,
    )

    assertThatThrownBy { draftReferralService.upsertCommunityServiceProvider(UUID.randomUUID(), request) }
      .isInstanceOf(NotFoundException::class.java)
  }

  @Test
  fun `update community service provider should throw not found for unknown community service provider`() {
    val referralUser = referralHelper.ensureReferralUser()
    val createReferralRequest = setUpData()

    val result = referralService.createReferral(referralUser.id, createReferralRequest)
    val savedReferral = result.referral

    val request = CommunityServiceProviderRequest(
      communityServiceProviderId = UUID.randomUUID(),
    )

    assertThatThrownBy { draftReferralService.upsertCommunityServiceProvider(savedReferral.id, request) }
      .isInstanceOf(NotFoundException::class.java)
  }

  @Test
  fun getAdditionalInformationForTheDeliveryPartnerSmokeTest() {
    val referralUser = referralHelper.ensureReferralUser()
    val createReferralRequest = setUpData()

    val referralCreationResult = referralService.createReferral(referralUser.id, createReferralRequest)
    val savedReferral = referralCreationResult.referral
    // create the selection to the database
    val yesSelectedRequest = SelectionDto.Yes("extra information for delivery partner")
    val response1 = draftReferralService.updateAdditionalInformationForTheDeliveryPartner(
      savedReferral.id,
      yesSelectedRequest,
      OffsetDateTime.now(),
    )
    assertThat(response1.details).isEqualTo(SelectionDto.Yes("extra information for delivery partner"))

    // retrieve the selection from the database
    val response2 = draftReferralService.getAdditionalInformationForTheDeliveryPartner(savedReferral.id)
    assertThat(response2.details).isEqualTo(SelectionDto.Yes("extra information for delivery partner"))

    // update the selection in the database
    val noSelectionRequest = SelectionDto.No
    val response3 = draftReferralService.updateAdditionalInformationForTheDeliveryPartner(
      savedReferral.id,
      noSelectionRequest,
      OffsetDateTime.now(),
    )
    assertThat(response3.details).isEqualTo(SelectionDto.No)

    // check the selection in the database
    val response4 = draftReferralService.getAdditionalInformationForTheDeliveryPartner(savedReferral.id)
    assertThat(response4.details).isEqualTo(SelectionDto.No)
  }

  @Nested
  inner class AdditionalInformationForTheDeliveryPartner {

    @Test
    fun `should give unanswered when unpopulated in referral`() {
      val referralUser = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson(firstName = "Robert", lastName = "Smith")
      val referral = referralHelper.createDraftReferral(person = person, createdBy = referralUser.id)

      val result = draftReferralService.getAdditionalInformationForTheDeliveryPartner(referral.id)

      assertThat(result.refereeName.firstName).isEqualTo("Robert")
      assertThat(result.refereeName.lastName).isEqualTo("Smith")
      assertThat(result.refereeName.middleName).isNull()
      assertThat(result.details).isEqualTo(SelectionDto.Unanswered)
    }

    @Test
    fun `should give no when hasAdditionalInformationForTheDeliveryPartner is false`() {
      val referralUser = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson(firstName = "Robert", lastName = "Smith")
      val referral = referralHelper.createDraftReferral(person = person, createdBy = referralUser.id)

      draftReferralService.updateAdditionalInformationForTheDeliveryPartner(referral.id, SelectionDto.No, OffsetDateTime.now())

      val result = draftReferralService.getAdditionalInformationForTheDeliveryPartner(referral.id)

      assertThat(result.refereeName.firstName).isEqualTo("Robert")
      assertThat(result.refereeName.lastName).isEqualTo("Smith")
      assertThat(result.refereeName.middleName).isNull()
      assertThat(result.details).isEqualTo(SelectionDto.No)
    }

    @Test
    fun `should give yes with the stored value when hasAdditionalInformationForTheDeliveryPartner is true`() {
      val referralUser = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson(firstName = "Robert", lastName = "Smith")
      val referral = referralHelper.createDraftReferral(person = person, createdBy = referralUser.id)
      val value = "extra information for delivery partner"

      draftReferralService.updateAdditionalInformationForTheDeliveryPartner(referral.id, SelectionDto.Yes(value), OffsetDateTime.now())

      val result = draftReferralService.getAdditionalInformationForTheDeliveryPartner(referral.id)

      assertThat(result.refereeName.firstName).isEqualTo("Robert")
      assertThat(result.refereeName.lastName).isEqualTo("Smith")
      assertThat(result.refereeName.middleName).isNull()
      assertThat(result.details).isEqualTo(SelectionDto.Yes(value))
    }

    @Test
    fun `updateAdditionalInformationForTheDeliveryPartner with no additional information should be saved`() {
      val referralUser = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson(firstName = "Robert", lastName = "Smith")
      val referral = referralHelper.createDraftReferral(person = person, createdBy = referralUser.id)
      val updatedAt = OffsetDateTime.parse("2026-08-26T12:00:00Z")

      val result = draftReferralService.updateAdditionalInformationForTheDeliveryPartner(referral.id, SelectionDto.No, updatedAt)

      val saved = referralRepository.findById(referral.id).get()
      assertThat(saved.id).isEqualTo(referral.id)
      assertThat(saved.personId).isEqualTo(person.id)
      assertThat(saved.personIdentifier).isEqualTo(person.identifier)
      assertThat(saved.createdBy).isEqualTo(referralUser.id)
      assertThat(saved.updatedAt).isEqualTo(updatedAt)
      assertThat(saved.hasAdditionalInformationForTheDeliveryPartner).isFalse()
      assertThat(saved.additionalInformationForTheDeliveryPartner).isNull()

      assertThat(result.refereeName.firstName).isEqualTo("Robert")
      assertThat(result.refereeName.lastName).isEqualTo("Smith")
      assertThat(result.refereeName.middleName).isNull()
      assertThat(result.details).isEqualTo(SelectionDto.No)
    }

    @Test
    fun `should throw when referral is in an invalid selection state`() {
      val referralUser = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson(firstName = "Robert", lastName = "Smith")
      val referral = referralHelper.createDraftReferral(person = person, createdBy = referralUser.id)

      referral.hasAdditionalInformationForTheDeliveryPartner = true
      referral.additionalInformationForTheDeliveryPartner = null
      referralRepository.save(referral)

      assertThatThrownBy { draftReferralService.getAdditionalInformationForTheDeliveryPartner(referral.id) }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessage("Invalid Selection state: selected=true, value=null")
    }
  }

  @Nested
  inner class UpsertProbationPractitionerDetails {

    @Test
    fun `should create a new record including the phone number when none exists`() {
      val referralUser = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson()
      val referral = referralHelper.createDraftReferral(person = person, createdBy = referralUser.id)

      val request = UpdateProbationPractitionerDetailsRequest(
        name = "Jane Doe",
        jobRole = "Probation practitioner",
        emailAddress = "jane.doe@example.com",
        pduId = COUNTY_DURHAM_AND_DARLINGTON_PDU_ID,
        probationOfficeId = 1,
        teamPhoneNumber = "0123456789",
        phoneNumber = "0987654321",
        ppDetailsFoundAndCorrect = true,
      )

      val result = draftReferralService.upsertProbationPractitionerDetails(referral.id, referralUser.id, request)

      val saved = probationPractitionerDetailsRepository.findByReferralId(referral.id)
      assertThat(saved).isNotNull()
      assertThat(saved?.referralId).isEqualTo(referral.id)
      assertThat(saved?.name).isEqualTo("Jane Doe")
      assertThat(saved?.pdu).isEqualTo(COUNTY_DURHAM_AND_DARLINGTON_PDU_ID)
      assertThat(saved?.probationOffice).isEqualTo(1)
      assertThat(saved?.teamPhoneNumber).isEqualTo("0123456789")
      assertThat(saved?.phoneNumber).isEqualTo("0987654321")
      assertThat(saved?.updatedBy).isEqualTo(referralUser.id)

      assertThat(result.pdu).isEqualTo(Pdu(id = COUNTY_DURHAM_AND_DARLINGTON_PDU_ID, name = "County Durham and Darlington"))
      assertThat(result.probationOffice).isEqualTo("Derby: Derwent Centre")
      assertThat(result.phoneNumber).isEqualTo("0987654321")
      assertThat(result.teamPhoneNumber).isEqualTo("0123456789")
    }

    @Test
    fun `should update the phone number on an existing record`() {
      val referralUser = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson()
      val referral = referralHelper.createDraftReferral(person = person, createdBy = referralUser.id)

      probationPractitionerDetailsRepository.save(
        ProbationPractitionerDetails(
          id = UUID.randomUUID(),
          referralId = referral.id,
          name = "Jane Doe",
          phoneNumber = "0000000000",
          updatedAt = OffsetDateTime.now(),
          updatedBy = referralUser.id,
        ),
      )

      val request = UpdateProbationPractitionerDetailsRequest(
        name = "Jane Doe",
        phoneNumber = "0987654321",
      )

      val result = draftReferralService.upsertProbationPractitionerDetails(referral.id, referralUser.id, request)

      val updated = probationPractitionerDetailsRepository.findByReferralId(referral.id)
      assertThat(updated).isNotNull()
      assertThat(updated?.phoneNumber).isEqualTo("0987654321")
      assertThat(updated?.updatedBy).isEqualTo(referralUser.id)
      assertThat(result.phoneNumber).isEqualTo("0987654321")
    }
  }

  private fun setUpData(): CreateReferralRequest {
    val crn = "X123456"
    stubFor(
      get(urlPathEqualTo("/person/probation/$crn"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(createCprProbationPersonDto(crn).toJson()),
        ),
    )
    return CreateReferralRequest(personIdentifier = crn)
  }
}
