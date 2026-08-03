package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralCriminogenicNeeds
import java.time.OffsetDateTime
import java.util.UUID

data class ReferralCriminogenicNeedsDto(
  val id: UUID,
  val referralId: UUID,
  val hasAccommodationNeeds: Boolean? = null,
  val accommodationDetails: String? = null,
  val hasEmploymentEducationNeeds: Boolean? = null,
  val employmentEducationDetails: String? = null,
  val hasFinancialNeeds: Boolean? = null,
  val financialDetails: String? = null,
  val hasPersonalRelationshipsCommunityNeeds: Boolean? = null,
  val personalRelationshipsCommunityDetails: String? = null,
  val hasDrugUseNeeds: Boolean? = null,
  val drugUseDetails: String? = null,
  val hasAlcoholUseNeeds: Boolean? = null,
  val alcoholUseDetails: String? = null,
  val hasHealthWellbeingNeeds: Boolean? = null,
  val healthWellbeingDetails: String? = null,
  val hasThinkingBehavioursAttitudeNeeds: Boolean? = null,
  val thinkingBehavioursAttitudeDetails: String? = null,
  val updatedAt: OffsetDateTime,
  val updatedBy: UUID,
) {
  companion object {
    fun from(referralCriminogenicNeeds: ReferralCriminogenicNeeds): ReferralCriminogenicNeedsDto = ReferralCriminogenicNeedsDto(
      id = referralCriminogenicNeeds.id,
      referralId = referralCriminogenicNeeds.referral.id,
      hasAccommodationNeeds = referralCriminogenicNeeds.hasAccommodationNeeds,
      accommodationDetails = referralCriminogenicNeeds.accommodationDetails,
      hasEmploymentEducationNeeds = referralCriminogenicNeeds.hasEmploymentEducationNeeds,
      employmentEducationDetails = referralCriminogenicNeeds.employmentEducationDetails,
      hasFinancialNeeds = referralCriminogenicNeeds.hasFinancialNeeds,
      financialDetails = referralCriminogenicNeeds.financialDetails,
      hasPersonalRelationshipsCommunityNeeds = referralCriminogenicNeeds.hasPersonalRelationshipsCommunityNeeds,
      personalRelationshipsCommunityDetails = referralCriminogenicNeeds.personalRelationshipsCommunityDetails,
      hasDrugUseNeeds = referralCriminogenicNeeds.hasDrugUseNeeds,
      drugUseDetails = referralCriminogenicNeeds.drugUseDetails,
      hasAlcoholUseNeeds = referralCriminogenicNeeds.hasAlcoholUseNeeds,
      alcoholUseDetails = referralCriminogenicNeeds.alcoholUseDetails,
      hasHealthWellbeingNeeds = referralCriminogenicNeeds.hasHealthWellbeingNeeds,
      healthWellbeingDetails = referralCriminogenicNeeds.healthWellbeingDetails,
      hasThinkingBehavioursAttitudeNeeds = referralCriminogenicNeeds.hasThinkingBehavioursAttitudeNeeds,
      thinkingBehavioursAttitudeDetails = referralCriminogenicNeeds.thinkingBehavioursAttitudeDetails,
      updatedAt = referralCriminogenicNeeds.updatedAt,
      updatedBy = referralCriminogenicNeeds.updatedBy,
    )
  }
}
