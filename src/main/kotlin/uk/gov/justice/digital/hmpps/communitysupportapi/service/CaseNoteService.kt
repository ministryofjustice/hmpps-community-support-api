package uk.gov.justice.digital.hmpps.communitysupportapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SubmitCaseNoteRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SuccessMessageResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNote
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralNotesRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CaseNoteService(
  private val referralNoteRepository: ReferralNotesRepository,
  private val referralRepository: ReferralRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun submitCaseNote(
    referralReference: String,
    request: SubmitCaseNoteRequest,
    createdBy: String,
  ): SuccessMessageResponse {
    val createdAt = OffsetDateTime.now()
    val referral = referralRepository
      .findByReferenceNumber(referralReference)
      .firstOrNull()
      ?: throw NotFoundException("Referral not found for reference $referralReference")

    referralNoteRepository.save(
      ReferralNote(
        id = UUID.randomUUID(),
        referralId = referral.id,
        createdAt = createdAt,
        createdBy = createdBy,
        title = request.title,
        details = request.details,
        shouldNotify = request.shouldNotify,
      ),
    )

    logger.info(
      "Successfully submitted a case note for referral {}",
      referralReference,
    )

    return SuccessMessageResponse(
      success = true,
      message = "Case note submitted successfully",
    )
  }
}
