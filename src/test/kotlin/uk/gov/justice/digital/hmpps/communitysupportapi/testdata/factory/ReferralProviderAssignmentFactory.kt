package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataCommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralProviderAssignment
import java.time.LocalDateTime
import java.util.UUID

class ReferralProviderAssignmentFactory : TestEntityFactory<ReferralProviderAssignment>() {

  private var id: UUID = UUID.randomUUID()
  private lateinit var referral: Referral
  private lateinit var referenceDataCommunityServiceProvider: ReferenceDataCommunityServiceProvider
  private var createdAt: LocalDateTime = LocalDateTime.now()

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferral(referral: Referral) = apply { this.referral = referral }
  fun withCommunityServiceProvider(referenceDataCommunityServiceProvider: ReferenceDataCommunityServiceProvider) = apply {
    this.referenceDataCommunityServiceProvider = referenceDataCommunityServiceProvider
  }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }

  override fun create(): ReferralProviderAssignment {
    check(::referral.isInitialized) { "Referral must be set before creating ReferralProviderAssignment" }
    check(::referenceDataCommunityServiceProvider.isInitialized) { "CommunityServiceProvider must be set before creating ReferralProviderAssignment" }

    return ReferralProviderAssignment(
      id = id,
      referral = referral,
      referenceDataCommunityServiceProvider = referenceDataCommunityServiceProvider,
      createdAt = createdAt,
    )
  }

  companion object {
    fun anAssignment(referral: Referral, provider: ReferenceDataCommunityServiceProvider): ReferralProviderAssignment = ReferralProviderAssignmentFactory()
      .withReferral(referral)
      .withCommunityServiceProvider(provider)
      .create()
  }
}
