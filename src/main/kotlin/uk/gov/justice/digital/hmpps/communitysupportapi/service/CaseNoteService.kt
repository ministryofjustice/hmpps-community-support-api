package uk.gov.justice.digital.hmpps.communitysupportapi.service

import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SubmitCaseNoteRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SuccessMessageResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataReferralPathway
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNote
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNotesReferenceDataReferralPathway
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferenceDataReferralPathwayRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralNotesReferenceDataReferralPathwayRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralNotesRepository
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CaseNoteService(
  private val referralNoteRepository: ReferralNotesRepository,
  private val referralLookupService: ReferralLookupService,
  private val referralNotesReferenceDataReferralPathwayRepository: ReferralNotesReferenceDataReferralPathwayRepository,
  private val referenceDataReferralPathwayRepository: ReferenceDataReferralPathwayRepository,
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
    val referral = referralLookupService.findByCaseIdentifier(referralReference)

    val pathwayIds = referenceDataReferralPathwayRepository.findAll().map(ReferenceDataReferralPathway::id)
    if (request.pathways.isEmpty()) {
      throw ValidationException("A case note must have at least one pathway to submit")
    }
    if (!pathwayIds.containsAll(request.pathways)) {
      throw ValidationException("One of ${request.pathways} is not a valid pathway ID")
    }

    val note = referralNoteRepository.save(
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

    val referralPathways = request.pathways.map { pathwayId ->
      ReferralNotesReferenceDataReferralPathway(
        id = UUID.randomUUID(),
        referenceDataReferralPathwayId = pathwayId,
        referralNoteId = note.id,
      )
    }

    referralNotesReferenceDataReferralPathwayRepository.saveAll(referralPathways)

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
