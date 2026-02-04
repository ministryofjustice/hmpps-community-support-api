package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralProviderAssignment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import java.time.LocalDateTime
import java.util.UUID

class ReferralProviderAssignmentFactory : TestEntityFactory<ReferralProviderAssignment>() {

  private var id: UUID = UUID.randomUUID()
  private lateinit var referral: Referral
  private lateinit var communityServiceProvider: CommunityServiceProvider
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var createdBy: ReferralUser? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferral(referral: Referral) = apply { this.referral = referral }
  fun withCommunityServiceProvider(communityServiceProvider: CommunityServiceProvider) = apply {
    this.communityServiceProvider = communityServiceProvider
  }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withCreatedBy(createdBy: ReferralUser?) = apply { this.createdBy = createdBy }

  override fun create(): ReferralProviderAssignment {
    check(::referral.isInitialized) { "Referral must be set before creating ReferralProviderAssignment" }
    check(::communityServiceProvider.isInitialized) { "CommunityServiceProvider must be set before creating ReferralProviderAssignment" }

    return ReferralProviderAssignment(
      id = id,
      referral = referral,
      communityServiceProvider = communityServiceProvider,
      createdAt = createdAt,
      createdBy = createdBy,
    )
  }

  companion object {
    fun anAssignment(referral: Referral, provider: CommunityServiceProvider): ReferralProviderAssignment = ReferralProviderAssignmentFactory()
      .withReferral(referral)
      .withCommunityServiceProvider(provider)
      .create()
  }
}
