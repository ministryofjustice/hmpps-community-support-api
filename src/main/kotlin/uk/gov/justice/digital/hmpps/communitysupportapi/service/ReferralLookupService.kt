package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CaseIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.validation.CaseIdentifierValidator

@Service
class ReferralLookupService(
  private val referralRepository: ReferralRepository,
  private val caseIdentifierValidator: CaseIdentifierValidator,
) {
  /**
   * Resolves the given [caseIdentifier] to a [Referral], throwing [NotFoundException] if it
   * cannot be found and [jakarta.validation.ValidationException] if the format is invalid.
   */
  fun findByCaseIdentifier(caseIdentifier: String?): Referral = when (val identifier = caseIdentifierValidator.validate(caseIdentifier)) {
    is CaseIdentifier.ReferralId -> referralRepository.findById(identifier.value)
      .orElseThrow { NotFoundException("Referral not found for id ${identifier.value}") }

    is CaseIdentifier.CaseId -> referralRepository.findReferenceNumberOrNull(identifier.value)
      ?: throw NotFoundException("Referral not found for reference ${identifier.value}")
  }
}
