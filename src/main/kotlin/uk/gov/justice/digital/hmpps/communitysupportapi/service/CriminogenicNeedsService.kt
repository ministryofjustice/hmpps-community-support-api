package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralCriminogenicNeedsDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralCriminogenicNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CriminogenicNeedsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralCriminogenicNeedsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CriminogenicNeedsService(
  private val referralRepository: ReferralRepository,
  private val referralCriminogenicNeedsRepository: ReferralCriminogenicNeedsRepository,
) {

  fun getCriminogenicNeeds(referralId: UUID): ReferralCriminogenicNeedsDto {
    val referral = ensureReferralExists(referralId)
    return ReferralCriminogenicNeedsDto.from(ensureCriminogenicNeedsExist(referral.id))
  }

  @Transactional
  fun upsertCriminogenicNeeds(referralId: UUID, userId: UUID, request: CriminogenicNeedsRequest): ReferralCriminogenicNeedsDto {
    val normalisedRequest = request.validateAndNormalise()
    val referral = ensureReferralExists(referralId)
    val existingRecord = findCriminogenicNeeds(referral.id)

    val criminogenicNeeds = ReferralCriminogenicNeeds(
      id = existingRecord?.id ?: UUID.randomUUID(),
      referral = referral,
      hasAccommodationNeeds = normalisedRequest.hasAccommodationNeeds,
      accommodationDetails = normalisedRequest.accommodationDetails,
      hasEmploymentEducationNeeds = normalisedRequest.hasEmploymentEducationNeeds,
      employmentEducationDetails = normalisedRequest.employmentEducationDetails,
      hasFinancialNeeds = normalisedRequest.hasFinancialNeeds,
      financialDetails = normalisedRequest.financialDetails,
      hasPersonalRelationshipsCommunityNeeds = normalisedRequest.hasPersonalRelationshipsCommunityNeeds,
      personalRelationshipsCommunityDetails = normalisedRequest.personalRelationshipsCommunityDetails,
      hasDrugUseNeeds = normalisedRequest.hasDrugUseNeeds,
      drugUseDetails = normalisedRequest.drugUseDetails,
      hasAlcoholUseNeeds = normalisedRequest.hasAlcoholUseNeeds,
      alcoholUseDetails = normalisedRequest.alcoholUseDetails,
      hasHealthWellbeingNeeds = normalisedRequest.hasHealthWellbeingNeeds,
      healthWellbeingDetails = normalisedRequest.healthWellbeingDetails,
      hasThinkingBehavioursAttitudeNeeds = normalisedRequest.hasThinkingBehavioursAttitudeNeeds,
      thinkingBehavioursAttitudeDetails = normalisedRequest.thinkingBehavioursAttitudeDetails,
      updatedAt = OffsetDateTime.now(),
      updatedBy = userId,
    )

    val savedCriminogenicNeedsRecord = referralCriminogenicNeedsRepository.save(criminogenicNeeds)
    return ReferralCriminogenicNeedsDto.from(savedCriminogenicNeedsRecord)
  }

  private fun ensureReferralExists(referralId: UUID): Referral {
    return referralRepository.findById(referralId)
      .orElseThrow { NotFoundException("Referral not found for id $referralId") }
  }

  private fun findCriminogenicNeeds(referralId: UUID): ReferralCriminogenicNeeds? {
    return referralCriminogenicNeedsRepository.findByReferralId(referralId)
  }

  private fun ensureCriminogenicNeedsExist(referralId: UUID): ReferralCriminogenicNeeds {
    return findCriminogenicNeeds(referralId)
      ?: throw NotFoundException("Criminogenic needs not found for referral $referralId")
  }
}
