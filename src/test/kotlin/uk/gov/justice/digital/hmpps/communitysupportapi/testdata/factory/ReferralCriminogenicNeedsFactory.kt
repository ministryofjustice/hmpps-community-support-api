package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.h
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralCriminogenicNeeds
import java.time.OffsetDateTime
import java.util.UUID

class ReferralCriminogenicNeedsFactory: TestEntityFactory<ReferralCriminogenicNeeds>() {
  private var id: UUID = UUID.randomUUID()
  private lateinit var referral: Referral
  private var hasAccommodationNeeds: Boolean? = null
  private var accommodationDetails: String? = null
  private var hasEmploymentEducationNeeds: Boolean? = null
  private var employmentEducationDetails: String? = null
  private var hasFinancialNeeds: Boolean? = null
  private var financialDetails: String? = null
  private var hasPersonalRelationshipsCommunityNeeds: Boolean? = null
  private var personalRelationshipsCommunityDetails: String? = null
  private var hasDrugUseNeeds: Boolean? = null
  private var drugUseDetails: String? = null
  private var hasAlcoholUseNeeds: Boolean? = null
  private var alcoholUseDetails: String? = null
  private var hasHealthWellbeingNeeds: Boolean? = null
  private var healthWellbeingDetails: String? = null
  private var hasThinkingBehavioursAttitudeNeeds: Boolean? = null
  private var thinkingBehavioursAttitudeDetails: String? = null
  private var updatedAt: OffsetDateTime  = OffsetDateTime.now()
  private var updatedBy: UUID = UUID.randomUUID()

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferral(referral: Referral) = apply { this.referral = referral }

  fun withUpdatedAt(updatedAt: OffsetDateTime) = apply { this.updatedAt = updatedAt }
  fun withUpdatedBy(userId: UUID) = apply { this.updatedBy = userId }

  override fun create(): ReferralCriminogenicNeeds = ReferralCriminogenicNeeds(id, referral,
    hasAccommodationNeeds,
    accommodationDetails,
    hasEmploymentEducationNeeds,
    employmentEducationDetails,
    hasFinancialNeeds,
    financialDetails,
    hasPersonalRelationshipsCommunityNeeds,
    personalRelationshipsCommunityDetails,
    hasDrugUseNeeds,
    drugUseDetails,
    hasAlcoholUseNeeds,
    alcoholUseDetails,
    hasHealthWellbeingNeeds,
    healthWellbeingDetails,
    hasThinkingBehavioursAttitudeNeeds,
    thinkingBehavioursAttitudeDetails,
    updatedAt,
    updatedBy)
}