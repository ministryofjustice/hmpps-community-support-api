package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AdditionalSupportNeedsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.NeedsInterpreterBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.TaskListStatusResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AdditionalSupportNeedsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.NeedsInterpreterRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonAdditionalSupportNeedsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.time.OffsetDateTime
import java.util.UUID

@Service
class DraftReferralService(
  private val referralRepository: ReferralRepository,
  private val personRepository: PersonRepository,
  private val personAdditionalSupportNeedsRepository: PersonAdditionalSupportNeedsRepository,
) {
  private data class ReferralSupportNeedsContext(
    val referral: Referral,
    val person: Person,
    val additionalSupportNeeds: PersonAdditionalSupportNeeds?,
  )

  companion object {
    private val logger = LoggerFactory.getLogger(DraftReferralService::class.java)
  }

  fun getReferral(referralId: UUID) = referralRepository.findById(referralId)

  fun getAdditionalSupportNeedsForReferral(
    referralId: String,
  ): AdditionalSupportNeedsBffResponseDto {
    val context = getReferralSupportNeedsContext(UUID.fromString(referralId))

    return context.additionalSupportNeeds?.let {
      AdditionalSupportNeedsBffResponseDto.from(context.person, it)
    } ?: throw NotFoundException("Personal additional support needs not found for referral $referralId")
  }

  fun getInterpreterNeedsForReferral(
    referralId: String,
  ): NeedsInterpreterBffResponseDto {
    val context = getReferralSupportNeedsContext(UUID.fromString(referralId))

    return context.additionalSupportNeeds?.let {
      NeedsInterpreterBffResponseDto.from(context.person, it)
    } ?: throw NotFoundException("Interpreter needs not found for referral $referralId")
  }

  @Transactional
  fun upsertAdditionalSupportNeeds(
    referralId: UUID,
    userId: UUID,
    request: AdditionalSupportNeedsRequest,
  ): AdditionalSupportNeedsBffResponseDto {
    val context = getReferralSupportNeedsContext(referralId)

    val personAdditionalSupportNeeds = if (context.additionalSupportNeeds == null) {
      createSupportNeeds(referralId, context.person.id, request, userId)
    } else {
      updateSupportNeeds(context.additionalSupportNeeds, request, userId)
    }

    return AdditionalSupportNeedsBffResponseDto.from(context.person, personAdditionalSupportNeeds)
  }

  @Transactional
  fun upsertNeedsInterpreter(
    referralId: UUID,
    userId: UUID,
    request: NeedsInterpreterRequest,
  ): NeedsInterpreterBffResponseDto {
    val context = getReferralSupportNeedsContext(referralId)

    val personAdditionalSupportNeeds = if (context.additionalSupportNeeds == null) {
      createNeedsInterpreter(referralId, context.person.id, request, userId)
    } else {
      updateNeedsInterpreter(context.additionalSupportNeeds, request, userId)
    }

    return NeedsInterpreterBffResponseDto.from(context.person, personAdditionalSupportNeeds)
  }

  private fun createSupportNeeds(
    referralId: UUID,
    personId: UUID,
    request: AdditionalSupportNeedsRequest,
    createdBy: UUID,
  ): PersonAdditionalSupportNeeds {
    val normalisedRequest = request.normaliseAgainstNeedsAdditionalSupport()
    val supportNeeds = PersonAdditionalSupportNeeds(
      id = UUID.randomUUID(),
      referralId = referralId,
      personId = personId,
      noAdditionalSupportNeeded = !normalisedRequest.needsAdditionalSupport,
      physicalHealthDetails = normalisedRequest.physicalHealth,
      mentalEmotionalHealthDetails = normalisedRequest.mentalEmotionalHealth,
      neurodiversityDetails = normalisedRequest.neurodiversity,
      locationTravelDetails = normalisedRequest.locationTravel,
      caringResponsibilitiesDetails = normalisedRequest.caringResponsibilities,
      employmentResponsibilitiesDetails = normalisedRequest.employmentResponsibilities,
      diversityDetails = normalisedRequest.diversity,
      anythingElseDetails = normalisedRequest.anythingElse,
      createdBy = createdBy,
      createdAt = OffsetDateTime.now(),
    )
    return personAdditionalSupportNeedsRepository.save(supportNeeds)
  }

  private fun updateSupportNeeds(
    existingRecord: PersonAdditionalSupportNeeds,
    newRecord: AdditionalSupportNeedsRequest,
    updatedBy: UUID,
  ): PersonAdditionalSupportNeeds {
    val normalisedRequest = newRecord.normaliseAgainstNeedsAdditionalSupport()
    val noAdditionalSupportNeeded = !normalisedRequest.needsAdditionalSupport
    val supportNeeds = PersonAdditionalSupportNeeds(
      id = existingRecord.id,
      referralId = existingRecord.referralId,
      personId = existingRecord.personId,
      noAdditionalSupportNeeded = noAdditionalSupportNeeded,
      physicalHealthDetails = normalisedRequest.physicalHealth,
      mentalEmotionalHealthDetails = normalisedRequest.mentalEmotionalHealth,
      neurodiversityDetails = normalisedRequest.neurodiversity,
      locationTravelDetails = normalisedRequest.locationTravel,
      caringResponsibilitiesDetails = normalisedRequest.caringResponsibilities,
      employmentResponsibilitiesDetails = normalisedRequest.employmentResponsibilities,
      diversityDetails = normalisedRequest.diversity,
      anythingElseDetails = normalisedRequest.anythingElse,
      createdBy = existingRecord.createdBy,
      createdAt = existingRecord.createdAt,
      updatedBy = updatedBy,
      updatedAt = OffsetDateTime.now(),
    )
    return personAdditionalSupportNeedsRepository.save(supportNeeds)
  }

  private fun createNeedsInterpreter(
    referralId: UUID,
    personId: UUID,
    request: NeedsInterpreterRequest,
    createdBy: UUID,
  ): PersonAdditionalSupportNeeds {
    val normalisedRequest = request.normaliseAgainstNeedsInterpreter()
    val supportNeeds = PersonAdditionalSupportNeeds(
      id = UUID.randomUUID(),
      referralId = referralId,
      personId = personId,
      interpreterLanguage = normalisedRequest.language,
      createdBy = createdBy,
      createdAt = OffsetDateTime.now(),
    )
    return personAdditionalSupportNeedsRepository.save(supportNeeds)
  }

  private fun updateNeedsInterpreter(
    existingRecord: PersonAdditionalSupportNeeds,
    newRecord: NeedsInterpreterRequest,
    updatedBy: UUID,
  ): PersonAdditionalSupportNeeds {
    val normalisedRecord = newRecord.normaliseAgainstNeedsInterpreter()
    val copyRecord = existingRecord.copy(
      interpreterLanguage = normalisedRecord.language,
      updatedBy = updatedBy,
      updatedAt = OffsetDateTime.now(),
    )
    return personAdditionalSupportNeedsRepository.save(copyRecord)
  }

  private fun getReferralSupportNeedsContext(referralId: UUID): ReferralSupportNeedsContext {
    val referral = referralRepository.findById(referralId)
      .orElseThrow { NotFoundException("Referral not found for id $referralId") }

    val person = personRepository.findById(referral.personId)
      .orElseThrow { NotFoundException("Person not found for referral $referralId") }

    val additionalSupportNeeds = personAdditionalSupportNeedsRepository.findByReferralId(referralId)

    return ReferralSupportNeedsContext(
      referral = referral,
      person = person,
      additionalSupportNeeds = additionalSupportNeeds,
    )
  }

  fun getTaskListStatus(referralId: UUID): TaskListStatusResponseDto? {
    val referral = referralRepository.findById(referralId)
      .orElseThrow { NotFoundException("Referral not found for id $referralId") }

    val person = personRepository.findById(referral.personId)
      .orElseThrow { NotFoundException("Person not found for referral $referralId") }

    return TaskListStatusResponseDto(
      fullName = person.firstName + " " + person.lastName,
      confirmPersonalDetailsCompleted = false,
      checkRiskInformationCompleted = false,
      selectThePersonsNeedsCompleted = false,
      addDetailsOfAnyAdditionalSupportNeedsCompleted = false,
      addDetailsOfMainPointOfContactCompleted = false,
    )
  }
}
