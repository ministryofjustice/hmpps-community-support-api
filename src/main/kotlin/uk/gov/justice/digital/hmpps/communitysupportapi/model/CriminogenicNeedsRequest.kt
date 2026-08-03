package uk.gov.justice.digital.hmpps.communitysupportapi.model

import jakarta.validation.ValidationException

data class CriminogenicNeedsRequest(
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
) {
  fun validateAndNormalise(): CriminogenicNeedsRequest {
    val missingDetailFields = mutableListOf<String>()

    validateDetailRequirement(hasAccommodationNeeds, accommodationDetails, "accommodationDetails", missingDetailFields)
    validateDetailRequirement(hasEmploymentEducationNeeds, employmentEducationDetails, "employmentEducationDetails", missingDetailFields)
    validateDetailRequirement(hasFinancialNeeds, financialDetails, "financialDetails", missingDetailFields)
    validateDetailRequirement(
      hasPersonalRelationshipsCommunityNeeds,
      personalRelationshipsCommunityDetails,
      "personalRelationshipsCommunityDetails",
      missingDetailFields,
    )
    validateDetailRequirement(hasDrugUseNeeds, drugUseDetails, "drugUseDetails", missingDetailFields)
    validateDetailRequirement(hasAlcoholUseNeeds, alcoholUseDetails, "alcoholUseDetails", missingDetailFields)
    validateDetailRequirement(hasHealthWellbeingNeeds, healthWellbeingDetails, "healthWellbeingDetails", missingDetailFields)
    validateDetailRequirement(
      hasThinkingBehavioursAttitudeNeeds,
      thinkingBehavioursAttitudeDetails,
      "thinkingBehavioursAttitudeDetails",
      missingDetailFields,
    )

    if (missingDetailFields.isNotEmpty()) {
      throw ValidationException("Missing required detail fields: ${missingDetailFields.joinToString(", ")}")
    }

    return copy(
      accommodationDetails = normaliseDetail(hasAccommodationNeeds, accommodationDetails),
      employmentEducationDetails = normaliseDetail(hasEmploymentEducationNeeds, employmentEducationDetails),
      financialDetails = normaliseDetail(hasFinancialNeeds, financialDetails),
      personalRelationshipsCommunityDetails = normaliseDetail(hasPersonalRelationshipsCommunityNeeds, personalRelationshipsCommunityDetails),
      drugUseDetails = normaliseDetail(hasDrugUseNeeds, drugUseDetails),
      alcoholUseDetails = normaliseDetail(hasAlcoholUseNeeds, alcoholUseDetails),
      healthWellbeingDetails = normaliseDetail(hasHealthWellbeingNeeds, healthWellbeingDetails),
      thinkingBehavioursAttitudeDetails = normaliseDetail(hasThinkingBehavioursAttitudeNeeds, thinkingBehavioursAttitudeDetails),
    )
  }

  private fun validateDetailRequirement(
    hasNeed: Boolean?,
    detail: String?,
    detailFieldName: String,
    missingDetailFields: MutableList<String>,
  ) {
    if (hasNeed == true && detail.isNullOrBlank()) missingDetailFields.add(detailFieldName)
  }

  private fun normaliseDetail(hasNeed: Boolean?, detail: String?): String? {
    return if (hasNeed == true) detail?.trim() else null
  }
}
