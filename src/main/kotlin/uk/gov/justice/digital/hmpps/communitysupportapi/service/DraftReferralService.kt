package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AdditionalSupportNeedsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CommunityServiceProviderBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.NeedsInterpreterBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.TaskListStatusResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralProviderAssignment
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AdditionalSupportNeedsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CommunityServiceProviderRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.NeedsInterpreterRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonAdditionalSupportNeedsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralCriminogenicNeedsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.RiskInformationRepository
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

@Service
class DraftReferralService(
  private val referralRepository: ReferralRepository,
  private val referralCriminogenicNeedsRepository: ReferralCriminogenicNeedsRepository,
  private val personRepository: PersonRepository,
  private val personAdditionalSupportNeedsRepository: PersonAdditionalSupportNeedsRepository,
  private val riskInformationRepository: RiskInformationRepository,
  private val communityServiceProviderRepository: CommunityServiceProviderRepository,
  private val referralProviderAssignmentRepository: ReferralProviderAssignmentRepository,
) {
  private data class ReferralSupportNeedsContext(
    val referral: Referral,
    val person: Person,
    val additionalSupportNeeds: PersonAdditionalSupportNeeds?,
  )

  companion object {
    private val logger = LoggerFactory.getLogger(DraftReferralService::class.java)
  }

  fun getAdditionalSupportNeedsForReferral(
    referralId: String,
  ): AdditionalSupportNeedsBffResponseDto {
    val context = getReferralSupportNeedsContext(UUID.fromString(referralId))

    context.additionalSupportNeeds?.let {
      return AdditionalSupportNeedsBffResponseDto.fromNeeds(context.person, it)
    }
    return AdditionalSupportNeedsBffResponseDto.fromPerson(context.person)
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

    return AdditionalSupportNeedsBffResponseDto.fromNeeds(context.person, personAdditionalSupportNeeds)
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

  @Transactional
  fun upsertCommunityServiceProvider(
    referralId: UUID,
    request: CommunityServiceProviderRequest,
  ): CommunityServiceProviderBffResponseDto {
    val referral = referralRepository.findById(referralId)
      .orElseThrow { NotFoundException("Referral not found for id $referralId") }

    val communityServiceProvider = communityServiceProviderRepository.findById(request.communityServiceProviderId)
      .orElseThrow { NotFoundException("Community Service Provider not found for id ${request.communityServiceProviderId}") }

    // This is a temporary solution to ensure that only one provider assignment exists for a referral.
    // IPB-2532 is done to remove providing community service provider from the referral entity.
    val existingAssignments = referralProviderAssignmentRepository.findByReferralId(referralId)
    if (existingAssignments.isNotEmpty()) {
      referralProviderAssignmentRepository.deleteAll(existingAssignments)
    }

    val providerAssignment = ReferralProviderAssignment(
      id = UUID.randomUUID(),
      referral = referral,
      communityServiceProvider = communityServiceProvider,
      createdAt = LocalDateTime.now(),
    )
    referralProviderAssignmentRepository.save(providerAssignment)

    return CommunityServiceProviderBffResponseDto.from(referralId, communityServiceProvider)
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
      additionalSupportNeeded = normalisedRequest.needsAdditionalSupport,
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
    val supportNeeds = PersonAdditionalSupportNeeds(
      id = existingRecord.id,
      referralId = existingRecord.referralId,
      personId = existingRecord.personId,
      additionalSupportNeeded = normalisedRequest.needsAdditionalSupport,
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
      interpreterNeeded = normalisedRequest.needsInterpreter,
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
      interpreterNeeded = normalisedRecord.needsInterpreter,
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

    val additionalSupportNeeds = personAdditionalSupportNeedsRepository.findByReferralId(referralId)

    val riskInfo = riskInformationRepository.findByReferralId(referralId)

    val criminogenicNeeds = referralCriminogenicNeedsRepository.findByReferralId(referralId)

    val person = personRepository.findById(referral.personId)
      .orElseThrow { NotFoundException("Person not found for referral $referralId") }

    return TaskListStatusResponseDto.from(person, referral, additionalSupportNeeds, riskInfo, criminogenicNeeds)
  }
}
