package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toJson
import java.time.OffsetDateTime
import java.util.UUID

class ServiceEndDatePageDtoTest {

  @Test
  fun `serializes with snake case field names`() {
    val response = ServiceEndDatePageDto(
      targetServiceCompletionDate = OffsetDateTime.parse("2026-11-13T10:09:02Z"),
      targetServiceCompletionReason = "Extended due to complexity",
    )

    val json = response.toJson()

    assertTrue(json.contains("\"target_service_completion_date\""))
    assertTrue(json.contains("\"target_service_completion_reason\""))
  }

  @Test
  fun `maps from referral`() {
    val referral = Referral(
      id = UUID.randomUUID(),
      personId = UUID.randomUUID(),
      personIdentifier = "X123456",
      createdAt = OffsetDateTime.parse("2026-08-13T10:09:02Z"),
      createdBy = UUID.randomUUID(),
      targetServiceCompletionDate = OffsetDateTime.parse("2026-11-13T10:09:02Z"),
      targetServiceCompletionDateReason = "Extended due to complexity",
    )

    val response = ServiceEndDatePageDto.from(referral)

    assertEquals(referral.targetServiceCompletionDate, response.targetServiceCompletionDate)
    assertEquals(referral.targetServiceCompletionDateReason, response.targetServiceCompletionReason)
  }
}
