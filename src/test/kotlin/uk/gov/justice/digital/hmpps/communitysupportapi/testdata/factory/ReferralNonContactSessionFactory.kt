package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNonContactSession
import java.time.OffsetDateTime
import java.util.UUID

class ReferralNonContactSessionFactory : TestEntityFactory<ReferralNonContactSession>() {

  private var id: UUID = UUID.randomUUID()
  private var referralId: UUID = UUID.randomUUID()
  private var createdAt: OffsetDateTime = OffsetDateTime.now()
  private var createdBy: String = "Service provider"
  private var deletedAt: OffsetDateTime? = null
  private var startDateTime: OffsetDateTime = OffsetDateTime.now()
  private var durationHours: Int = 1
  private var durationMinutes: Int = 30
  private var title: String = "Non contact session title"
  private var details: String = "Non contact session details"

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferralId(referralId: UUID) = apply { this.referralId = referralId }
  fun withCreatedAt(createdAt: OffsetDateTime) = apply { this.createdAt = createdAt }
  fun withCreatedBy(username: String) = apply { this.createdBy = username }
  fun withDeletedAt(deletedAt: OffsetDateTime) = apply { this.deletedAt = deletedAt }
  fun withStartDateTime(startDateTime: OffsetDateTime) = apply { this.startDateTime = startDateTime }
  fun withDurationHours(durationHours: Int) = apply { this.durationHours = durationHours }
  fun withDurationMinutes(durationMinutes: Int) = apply { this.durationMinutes = durationMinutes }
  fun withTitle(title: String) = apply { this.title = title }
  fun withDetails(details: String) = apply { this.details = details }

  override fun create(): ReferralNonContactSession = ReferralNonContactSession(
    id = this.id,
    referralId = this.referralId,
    createdAt = this.createdAt,
    createdBy = this.createdBy,
    deletedAt = this.deletedAt,
    startDateTime = this.startDateTime,
    durationHours = durationHours,
    durationMinutes = durationMinutes,
    title = this.title,
    details = details,
  )
}
