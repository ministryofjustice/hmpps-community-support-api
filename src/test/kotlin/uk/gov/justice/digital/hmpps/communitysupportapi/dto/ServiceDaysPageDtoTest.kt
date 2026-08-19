package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toJson
import java.time.OffsetDateTime
import java.util.UUID

class ServiceDaysPageDtoTest {

  @Test
  fun `serializes with snake case field name`() {
    val response = ServiceDaysPageDto(
      serviceDays = 45,
    )

    val json = response.toJson()

    assertTrue(json.contains("\"service_days\""))
  }

  @Test
  fun `maps from referral`() {
    val referral = Referral(
      id = UUID.randomUUID(),
      personId = UUID.randomUUID(),
      personIdentifier = "X123456",
      createdAt = OffsetDateTime.parse("2026-08-13T10:09:02Z"),
      createdBy = UUID.randomUUID(),
      serviceDays = 21,
    )

    val response = ServiceDaysPageDto.from(referral)

    assertEquals(referral.serviceDays, response.serviceDays)
  }
}
