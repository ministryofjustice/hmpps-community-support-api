package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AdditionalSupportNeedsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.NeedsInterpreterRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonAdditionalSupportNeedsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedDateOfBirth
import java.time.LocalDate
import java.util.UUID

class DraftReferralServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var draftReferralService: DraftReferralService

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var personAdditionSupportNeedsRepository: PersonAdditionalSupportNeedsRepository

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
  fun `should update person details confirmation`() {
    val user = referralHelper.ensureReferralUser()
    val createReferralRequest = setUpData()

    val result = referralService.createReferral(user.id, createReferralRequest)
    val referral = result.referral

    draftReferralService.confirmPersonDetails(referral.id, user.id)

    val updatedReferral = referralRepository.findById(result.referral.id).orElseThrow()

    assertThat(updatedReferral.personDetailsConfirmedBy).isEqualTo(user.id)
    assertThat(updatedReferral.personDetailsConfirmedAt).isNotNull()
  }

  @Test
  fun `should throw NotFoundException when referral does not exist`() {
    val unknownReferralId = UUID.randomUUID()
    val user = referralHelper.ensureReferralUser()

    assertThatThrownBy {
      draftReferralService.confirmPersonDetails(unknownReferralId, user.id)
    }
      .isInstanceOf(NotFoundException::class.java)
      .hasMessage("Referral not found for id $unknownReferralId")
  }

  private fun setUpData(): CreateReferralRequest {
    val communityServiceProvider = referralHelper.getCommunityServiceProvider()

    val person = PersonDto(
      id = UUID.randomUUID(),
      personIdentifier = "X123456",
      firstName = "John",
      lastName = "Smith",
      dateOfBirth = LocalDate.of(1980, 1, 1).toFormattedDateOfBirth(),
      sex = "Male",
      additionalDetails = null,
    )

    return CreateReferralRequest(
      communityServiceProviderId = communityServiceProvider.id,
      personIdentifier = person.personIdentifier.toString(),
    )
  }
}
