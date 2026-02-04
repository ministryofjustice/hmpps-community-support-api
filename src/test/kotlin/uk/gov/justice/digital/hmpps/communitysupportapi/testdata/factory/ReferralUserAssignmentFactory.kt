package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUserAssignment
import java.time.LocalDateTime
import java.util.UUID

class ReferralUserAssignmentFactory : TestEntityFactory<ReferralUserAssignment>() {

  private var id: UUID = UUID.randomUUID()
  private lateinit var referral: Referral
  private lateinit var user: ReferralUser
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var createdBy: ReferralUser? = null
  private var deletedAt: LocalDateTime? = null
  private var deletedBy: ReferralUser? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferral(referral: Referral) = apply { this.referral = referral }
  fun withUser(user: ReferralUser) = apply { this.user = user }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withCreatedBy(createdBy: ReferralUser?) = apply { this.createdBy = createdBy }
  fun withDeletedAt(deletedAt: LocalDateTime?) = apply { this.deletedAt = deletedAt }
  fun withDeletedBy(deletedBy: ReferralUser?) = apply { this.deletedBy = deletedBy }

  override fun create(): ReferralUserAssignment {
    check(::referral.isInitialized) { "Referral must be set before creating ReferralUserAssignment" }
    check(::user.isInitialized) { "User must be set before creating ReferralUserAssignment" }

    return ReferralUserAssignment(
      id = id,
      referral = referral,
      user = user,
      createdAt = createdAt,
      createdBy = createdBy,
      deletedAt = deletedAt,
      deletedBy = deletedBy,
    )
  }

  companion object {
    fun anAssignment(referral: Referral, user: ReferralUser): ReferralUserAssignment = ReferralUserAssignmentFactory()
      .withReferral(referral)
      .withUser(user)
      .create()

    fun aDeletedAssignment(referral: Referral, user: ReferralUser, deletedBy: ReferralUser): ReferralUserAssignment = ReferralUserAssignmentFactory()
      .withReferral(referral)
      .withUser(user)
      .withDeletedAt(LocalDateTime.now())
      .withDeletedBy(deletedBy)
      .create()
  }
}
