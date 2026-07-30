package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.RiskInformation
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
      val result = TaskListStatusResponseDto.from(person, referral, null, null)

      result.checkRiskInformationCompleted shouldBe TaskListStatusItem.notStarted()
    }

    @Test
    fun `returns completed for checkRiskInformationCompleted when risk info exists`() {
      val result = TaskListStatusResponseDto.from(person, referral, null, buildRiskInfo())

      result.checkRiskInformationCompleted shouldBe TaskListStatusItem.completed()
    }
  }

  @Nested
  inner class AdditionalSupportNeedsStatus {

    @Test
    fun `returns notStarted when no additionalSupportNeeds record exists`() {
      val result = TaskListStatusResponseDto.from(person, referral, null, null)

      result.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.notStarted()
    }

    @Test
    fun `returns inProgress when additionalSupportNeeded is null`() {
      val supportNeeds = buildSupportNeeds(additionalSupportNeeded = null, interpreterNeeded = null)

      val result = TaskListStatusResponseDto.from(person, referral, supportNeeds, null)

      result.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.inProgress()
    }

    @Test
    fun `returns inProgress when additionalSupportNeeded is true but interpreterNeeded is null`() {
      val supportNeeds = buildSupportNeeds(additionalSupportNeeded = true, interpreterNeeded = null)

      val result = TaskListStatusResponseDto.from(person, referral, supportNeeds, null)

      result.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.inProgress()
    }

    @Test
    fun `returns completed when additionalSupportNeeded is true and interpreterNeeded is false`() {
      val supportNeeds = buildSupportNeeds(additionalSupportNeeded = true, interpreterNeeded = false)

      val result = TaskListStatusResponseDto.from(person, referral, supportNeeds, null)

      result.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.completed()
    }

    @Test
    fun `returns completed when additionalSupportNeeded is false and interpreterNeeded is false`() {
      val supportNeeds = buildSupportNeeds(additionalSupportNeeded = false, interpreterNeeded = false)

      val result = TaskListStatusResponseDto.from(person, referral, supportNeeds, null)

      result.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.completed()
    }

    @Test
    fun `returns completed when both additionalSupportNeeded and interpreterNeeded are true`() {
      val supportNeeds = buildSupportNeeds(additionalSupportNeeded = true, interpreterNeeded = true)

      val result = TaskListStatusResponseDto.from(person, referral, supportNeeds, null)

      result.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.completed()
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

  private fun buildRiskInfo() = RiskInformation(
    id = UUID.randomUUID(),
    referralId = referralId,
    referral = referral,
    updatedAt = OffsetDateTime.now(),
    updatedBy = userId,
  )
}
