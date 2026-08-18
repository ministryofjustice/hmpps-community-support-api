package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ContractArea
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralCriminogenicNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Region
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.RiskInformation
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ServiceProvider
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class TaskListStatusResponseDtoTest {

  private val personId = UUID.randomUUID()
  private val referralId = UUID.randomUUID()
  private val userId = UUID.randomUUID()

  private val person = Person(
    id = personId,
    identifier = "CRN123",
    firstName = "Jane",
    lastName = "Doe",
    dateOfBirth = LocalDate.of(1990, 1, 1),
    gender = "Female",
  )

  private val referral = Referral(
    id = referralId,
    personId = personId,
    personIdentifier = "CRN123",
    createdAt = OffsetDateTime.now(),
    createdBy = userId,
  )

  @Nested
  inner class RiskInfoStatus {

    @Test
    fun `returns notStarted for checkRiskInformationCompleted when no risk info exists`() {
      val result = TaskListStatusResponseDto.from(referral, person, null, null, null, null)

      result.checkRiskInformationCompleted shouldBe TaskListStatusItem.notStarted()
    }

    @Test
    fun `returns completed for checkRiskInformationCompleted when risk info exists`() {
      val result = TaskListStatusResponseDto.from(referral, person, null, buildRiskInfo(), null, null)

      result.checkRiskInformationCompleted shouldBe TaskListStatusItem.completed()
    }
  }

  @Nested
  inner class AdditionalSupportNeedsStatus {

    @Test
    fun `returns notStarted when no additionalSupportNeeds record exists`() {
      val result = TaskListStatusResponseDto.from(referral, person, null, null, null, null)

      result.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.notStarted()
    }

    @Test
    fun `returns inProgress when additionalSupportNeeded is null`() {
      val supportNeeds = buildSupportNeeds(additionalSupportNeeded = null, interpreterNeeded = null)

      val result = TaskListStatusResponseDto.from(referral, person, supportNeeds, null, null, null)

      result.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.inProgress()
    }

    @Test
    fun `returns inProgress when additionalSupportNeeded is true but interpreterNeeded is null`() {
      val supportNeeds = buildSupportNeeds(additionalSupportNeeded = true, interpreterNeeded = null)

      val result = TaskListStatusResponseDto.from(referral, person, supportNeeds, null, null, null)

      result.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.inProgress()
    }

    @Test
    fun `returns completed when additionalSupportNeeded is true and interpreterNeeded is false`() {
      val supportNeeds = buildSupportNeeds(additionalSupportNeeded = true, interpreterNeeded = false)

      val result = TaskListStatusResponseDto.from(referral, person, supportNeeds, null, null, null)

      result.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.completed()
    }

    @Test
    fun `returns completed when additionalSupportNeeded is false and interpreterNeeded is false`() {
      val supportNeeds = buildSupportNeeds(additionalSupportNeeded = false, interpreterNeeded = false)

      val result = TaskListStatusResponseDto.from(referral, person, supportNeeds, null, null, null)

      result.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.completed()
    }

    @Test
    fun `returns completed when both additionalSupportNeeded and interpreterNeeded are true`() {
      val supportNeeds = buildSupportNeeds(additionalSupportNeeded = true, interpreterNeeded = true)

      val result = TaskListStatusResponseDto.from(referral, person, supportNeeds, null, null, null)

      result.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.completed()
    }
  }

  @Nested
  inner class CriminogenicNeedsStatus {

    @Test
    fun `returns notStarted when no criminogenicNeeds record exists`() {
      val result = TaskListStatusResponseDto.from(referral, person, null, null, null, null)

      result.selectThePersonsNeedsCompleted shouldBe TaskListStatusItem.notStarted()
    }

    @Test
    fun `returns completed when criminogenicNeeds record exists but no needs are selected`() {
      val criminogenicNeeds = buildCriminogenicNeeds(
        hasAccommodationNeeds = false,
        hasEmploymentEducationNeeds = false,
        hasFinancialNeeds = false,
        hasPersonalRelationshipsCommunityNeeds = false,
        hasDrugUseNeeds = false,
        hasAlcoholUseNeeds = false,
        hasHealthWellbeingNeeds = false,
        hasThinkingBehavioursAttitudeNeeds = false,
      )

      val result = TaskListStatusResponseDto.from(referral, person, null, null, criminogenicNeeds, null)

      result.selectThePersonsNeedsCompleted shouldBe TaskListStatusItem.completed()
    }

    @Test
    fun `returns completed when criminogenicNeeds has at least one need with details`() {
      val criminogenicNeeds = buildCriminogenicNeeds(
        hasAccommodationNeeds = true,
        accommodationDetails = "Needs stable accommodation",
      )

      val result = TaskListStatusResponseDto.from(referral, person, null, null, criminogenicNeeds, null)

      result.selectThePersonsNeedsCompleted shouldBe TaskListStatusItem.completed()
    }

    @Test
    fun `returns completed when criminogenicNeeds has multiple needs with details`() {
      val criminogenicNeeds = buildCriminogenicNeeds(
        hasAccommodationNeeds = true,
        accommodationDetails = "Needs stable accommodation",
        hasAlcoholUseNeeds = true,
        alcoholUseDetails = "Requires alcohol support",
        hasDrugUseNeeds = false,
      )

      val result = TaskListStatusResponseDto.from(referral, person, null, null, criminogenicNeeds, null)

      result.selectThePersonsNeedsCompleted shouldBe TaskListStatusItem.completed()
    }
  }

  @Nested
  inner class SelectAnAreaForReferralStatus {

    @Test
    fun `returns notStarted when no communityServiceProvider exists`() {
      val result = TaskListStatusResponseDto.from(referral, person, null, null, null, null)

      result.selectAnAreaForReferralCompleted shouldBe TaskListStatusItem.notStarted()
    }

    @Test
    fun `returns completed when communityServiceProvider exists`() {
      val communityServiceProvider = buildCommunityServiceProvider()

      val result = TaskListStatusResponseDto.from(referral, person, null, null, null, communityServiceProvider)

      result.selectAnAreaForReferralCompleted shouldBe TaskListStatusItem.completed()
    }
  }

  @Nested
  inner class AdditionalInformationStatus {

    @Test
    fun `returns notStarted when no target date or reason exists`() {
      val result = TaskListStatusResponseDto.from(referral, person, null, null, null, null)

      result.addAdditionalInformationCompleted shouldBe TaskListStatusItem.notStarted()
    }

    @Test
    fun `returns inProgress when only target date exists`() {
      val result = TaskListStatusResponseDto.from(
        buildReferralWithAdditionalInformation(
          targetServiceCompletionDate = OffsetDateTime.now(),
        ),
        person,
        null,
        null,
        null,
        null,
      )

      result.addAdditionalInformationCompleted shouldBe TaskListStatusItem.inProgress()
    }

    @Test
    fun `returns inProgress when only reason exists`() {
      val result = TaskListStatusResponseDto.from(
        buildReferralWithAdditionalInformation(
          targetServiceCompletionDateReason = "Some reason",
        ),
        person,
        null,
        null,
        null,
        null,
      )

      result.addAdditionalInformationCompleted shouldBe TaskListStatusItem.inProgress()
    }

    @Test
    fun `returns inProgress when only service days exists`() {
      val result = TaskListStatusResponseDto.from(
        buildReferralWithAdditionalInformation(
          serviceDays = 20,
        ),
        person,
        null,
        null,
        null,
        null,
      )

      result.addAdditionalInformationCompleted shouldBe TaskListStatusItem.inProgress()
    }

    @Test
    fun `returns inProgress when target date and reason exist without service days`() {
      val result = TaskListStatusResponseDto.from(
        buildReferralWithAdditionalInformation(
          targetServiceCompletionDate = OffsetDateTime.now(),
          targetServiceCompletionDateReason = "Some reason",
        ),
        person,
        null,
        null,
        null,
        null,
      )

      result.addAdditionalInformationCompleted shouldBe TaskListStatusItem.inProgress()
    }

    @Test
    fun `returns inProgress when target date and service days exist without reason`() {
      val result = TaskListStatusResponseDto.from(
        buildReferralWithAdditionalInformation(
          targetServiceCompletionDate = OffsetDateTime.now(),
          serviceDays = 20,
        ),
        person,
        null,
        null,
        null,
        null,
      )

      result.addAdditionalInformationCompleted shouldBe TaskListStatusItem.inProgress()
    }

    @Test
    fun `returns inProgress when reason and service days exist without target date`() {
      val result = TaskListStatusResponseDto.from(
        buildReferralWithAdditionalInformation(
          targetServiceCompletionDateReason = "Some reason",
          serviceDays = 20,
        ),
        person,
        null,
        null,
        null,
        null,
      )

      result.addAdditionalInformationCompleted shouldBe TaskListStatusItem.inProgress()
    }

    @Test
    fun `returns completed when target date reason and service days exist`() {
      val result = TaskListStatusResponseDto.from(
        buildReferralWithAdditionalInformation(
          targetServiceCompletionDate = OffsetDateTime.now(),
          targetServiceCompletionDateReason = "Some reason",
          serviceDays = 20,
        ),
        person,
        null,
        null,
        null,
        null,
      )

      result.addAdditionalInformationCompleted shouldBe TaskListStatusItem.completed()
    }
  }

  @Nested
  inner class TaskListStatusItemFactories {

    @Test
    fun `completed returns completed true with blue tag`() {
      val item = TaskListStatusItem.completed()

      item.completed shouldBe true
      item.statusText shouldBe "Completed"
      item.tag shouldBe "govuk-tag--blue"
    }

    @Test
    fun `notStarted returns completed false with grey tag`() {
      val item = TaskListStatusItem.notStarted()

      item.completed shouldBe false
      item.statusText shouldBe "Not started"
      item.tag shouldBe "govuk-tag--grey"
    }

    @Test
    fun `inProgress returns completed false with light-blue tag`() {
      val item = TaskListStatusItem.inProgress()

      item.completed shouldBe false
      item.statusText shouldBe "In progress"
      item.tag shouldBe "govuk-tag--light-blue"
    }
  }

  private fun buildSupportNeeds(additionalSupportNeeded: Boolean?, interpreterNeeded: Boolean?) = PersonAdditionalSupportNeeds(
    id = UUID.randomUUID(),
    referralId = referralId,
    personId = personId,
    additionalSupportNeeded = additionalSupportNeeded,
    interpreterNeeded = interpreterNeeded,
    createdBy = userId,
  )

  private fun buildReferralWithAdditionalInformation(
    targetServiceCompletionDate: OffsetDateTime? = null,
    targetServiceCompletionDateReason: String? = null,
    serviceDays: Int? = null,
  ) = Referral(
    id = referral.id,
    personId = referral.personId,
    personIdentifier = referral.personIdentifier,
    referenceNumber = referral.referenceNumber,
    createdAt = referral.createdAt,
    updatedAt = referral.updatedAt,
    urgency = referral.urgency,
    createdBy = referral.createdBy,
    targetServiceCompletionDate = targetServiceCompletionDate,
    targetServiceCompletionDateReason = targetServiceCompletionDateReason,
    serviceDays = serviceDays,
  )

  private fun buildRiskInfo() = RiskInformation(
    id = UUID.randomUUID(),
    referralId = referralId,
    referral = referral,
    updatedAt = OffsetDateTime.now(),
    updatedBy = userId,
  )

  private fun buildCriminogenicNeeds(
    hasAccommodationNeeds: Boolean? = null,
    accommodationDetails: String? = null,
    hasEmploymentEducationNeeds: Boolean? = null,
    employmentEducationDetails: String? = null,
    hasFinancialNeeds: Boolean? = null,
    financialDetails: String? = null,
    hasPersonalRelationshipsCommunityNeeds: Boolean? = null,
    personalRelationshipsCommunityDetails: String? = null,
    hasDrugUseNeeds: Boolean? = null,
    drugUseDetails: String? = null,
    hasAlcoholUseNeeds: Boolean? = null,
    alcoholUseDetails: String? = null,
    hasHealthWellbeingNeeds: Boolean? = null,
    healthWellbeingDetails: String? = null,
    hasThinkingBehavioursAttitudeNeeds: Boolean? = null,
    thinkingBehavioursAttitudeDetails: String? = null,
  ) = ReferralCriminogenicNeeds(
    id = UUID.randomUUID(),
    referral = referral,
    hasAccommodationNeeds = hasAccommodationNeeds,
    accommodationDetails = accommodationDetails,
    hasEmploymentEducationNeeds = hasEmploymentEducationNeeds,
    employmentEducationDetails = employmentEducationDetails,
    hasFinancialNeeds = hasFinancialNeeds,
    financialDetails = financialDetails,
    hasPersonalRelationshipsCommunityNeeds = hasPersonalRelationshipsCommunityNeeds,
    personalRelationshipsCommunityDetails = personalRelationshipsCommunityDetails,
    hasDrugUseNeeds = hasDrugUseNeeds,
    drugUseDetails = drugUseDetails,
    hasAlcoholUseNeeds = hasAlcoholUseNeeds,
    alcoholUseDetails = alcoholUseDetails,
    hasHealthWellbeingNeeds = hasHealthWellbeingNeeds,
    healthWellbeingDetails = healthWellbeingDetails,
    hasThinkingBehavioursAttitudeNeeds = hasThinkingBehavioursAttitudeNeeds,
    thinkingBehavioursAttitudeDetails = thinkingBehavioursAttitudeDetails,
    updatedAt = OffsetDateTime.now(),
    updatedBy = userId,
  )

  private fun buildCommunityServiceProvider() = CommunityServiceProvider(
    id = UUID.randomUUID(),
    contractArea = ContractArea(
      id = UUID.randomUUID(),
      region = Region(id = UUID.randomUUID(), name = "Test Region"),
      area = "Test Area",
    ),
    name = "Test Provider",
    serviceProvider = ServiceProvider(
      id = UUID.randomUUID(),
      authGroupId = "TEST_GROUP",
      name = "Test Service Provider",
    ),
    description = "Test Description",
  )
}
