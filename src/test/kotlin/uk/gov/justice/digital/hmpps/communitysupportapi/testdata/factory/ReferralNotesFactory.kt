package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNote
import java.time.OffsetDateTime
import java.util.UUID

class ReferralNotesFactory : TestEntityFactory<ReferralNote>() {

  private var id: UUID = UUID.randomUUID()
  private var referralId: UUID = UUID.randomUUID()
  private var createdAt: OffsetDateTime = OffsetDateTime.now()
  private var createdBy: String = "Service provider"
  private var deletedAt: OffsetDateTime? = null
  private var title: String = "Note title"
  private var details: String = "Note details"
  private var shouldNotify: Boolean = false

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferralId(referralId: UUID) = apply { this.referralId = referralId }
  fun withCreatedAt(createdAt: OffsetDateTime) = apply { this.createdAt = createdAt }
  fun withCreatedBy(createdBy: String) = apply { this.createdBy = createdBy }
  fun withDeletedAt(deletedAt: OffsetDateTime?) = apply { this.deletedAt = deletedAt }
  fun withTitle(title: String) = apply { this.title = title }
  fun withDetails(details: String) = apply { this.details = details }
  fun withShouldNotify(shouldNotify: Boolean) = apply { this.shouldNotify = shouldNotify }

  override fun create(): ReferralNote = ReferralNote(
    id = id,
    referralId = referralId,
    createdAt = createdAt,
    createdBy = createdBy,
    deletedAt = deletedAt,
    title = title,
    details = details,
    shouldNotify = shouldNotify,
  )
}
