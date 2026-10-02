package uk.gov.justice.digital.hmpps.communitysupportapi.util.placeholder

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.util.FULL_MONTH_DATE_FORMAT
import uk.gov.justice.digital.hmpps.communitysupportapi.util.PlaceholderUtils
import java.time.OffsetDateTime
import java.util.UUID

class ServiceEndDatePlaceholderTest {
  private val serviceEndDate = OffsetDateTime.now().plusDays(5)

  @Test
  fun `render should replace service end date using expected format`() {
    val referral = Referral(
      id = UUID.randomUUID(),
      personId = UUID.randomUUID(),
      personIdentifier = "X123456",
      createdAt = OffsetDateTime.now(),
      createdBy = UUID.randomUUID(),
      targetServiceCompletionDate = serviceEndDate,
    )

    val placeholder = ServiceEndDatePlaceholder(referral)

    Assertions.assertEquals(
      "Is the service end date still ${serviceEndDate.format(FULL_MONTH_DATE_FORMAT)}?",
      PlaceholderUtils.render("Is the service end date still {{ service_end_date }}?", placeholder),
    )
    Assertions.assertEquals(
      "Is the service end date still ${serviceEndDate.format(FULL_MONTH_DATE_FORMAT)}?",
      PlaceholderUtils.render("Is the service end date still {{ serviceEndDate }}?", placeholder),
    )
  }

  @Test
  fun `render should leave text unchanged when there is no placeholder parameter`() {
    val referral = Referral(
      id = UUID.randomUUID(),
      personId = UUID.randomUUID(),
      personIdentifier = "X123456",
      createdAt = OffsetDateTime.now(),
      createdBy = UUID.randomUUID(),
      targetServiceCompletionDate = serviceEndDate,
    )

    val placeholder = ServiceEndDatePlaceholder(referral)

    Assertions.assertEquals(
      "Is the service end date still set?",
      PlaceholderUtils.render("Is the service end date still set?", placeholder),
    )
  }

  @Test
  fun `resolve should skip missing target service completion date`() {
    val referral = Referral(
      id = UUID.randomUUID(),
      personId = UUID.randomUUID(),
      personIdentifier = "X123456",
      createdAt = OffsetDateTime.now(),
      createdBy = UUID.randomUUID(),
    )

    val placeholder = ServiceEndDatePlaceholder(referral)

    Assertions.assertEquals(emptyMap<String, String>(), placeholder.resolve(setOf("service_end_date")))
  }
}
