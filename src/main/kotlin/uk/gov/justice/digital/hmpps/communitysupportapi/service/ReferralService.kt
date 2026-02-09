package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralCreationResult
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralDetailsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SubmitReferralResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActorType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralProviderAssignment
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toEntity
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ReferralService(
  private val referralRepository: ReferralRepository,
  private val personRepository: PersonRepository,
  private val communityServiceProviderRepository: CommunityServiceProviderRepository,
  private val referralProviderAssignmentRepository: ReferralProviderAssignmentRepository,
  private val referenceGenerator: ReferralReferenceGenerator,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(ReferralService::class.java)
    private const val MAX_REFERENCE_NUMBER_TRIES = 10
  }

  fun getReferral(referralId: UUID) = referralRepository.findById(referralId)

  fun getReferralDetailsPage(referralId: UUID): ReferralDetailsBffResponseDto {
    val referral = referralRepository.findById(referralId).orElseThrow { NotFoundException("Referral not found for id $referralId") }
    val person = personRepository.findById(referral.personId).orElseThrow { NotFoundException("Person not found for referral ${referral.id}") }
    return ReferralDetailsBffResponseDto.from(referral, person)
  }

  fun createReferral(userId: UUID, createReferralRequest: CreateReferralRequest): ReferralCreationResult {
    val person = upsertPerson(createReferralRequest.personDetails)
    val communityServiceProvider = communityServiceProviderRepository.findById(createReferralRequest.communityServiceProviderId)
      .orElseThrow { NotFoundException("Community Service Provider not found for id ${createReferralRequest.communityServiceProviderId}") }

    val referralId = UUID.randomUUID()
    val now = OffsetDateTime.now()

    val referral = Referral(
      id = referralId,
      crn = createReferralRequest.crn,
      personId = person.id,
      createdAt = now,
      updatedAt = now,
      urgency = createReferralRequest.urgency,
    )

    val referralEvent = ReferralEvent(
      id = UUID.randomUUID(),
      eventType = ReferralEventType.CREATED,
      createdAt = now,
      actorType = ActorType.AUTH,
      actorId = userId,
      referral = referral,
    )

    referral.addEvent(referralEvent)
    val savedReferral = referralRepository.save(referral)

    // Create the provider assignment
    val providerAssignment = ReferralProviderAssignment(
      id = UUID.randomUUID(),
      referral = savedReferral,
      communityServiceProvider = communityServiceProvider,
      createdAt = LocalDateTime.now(),
    )
    referralProviderAssignmentRepository.save(providerAssignment)

    return ReferralCreationResult(
      referral = savedReferral,
      person = person,
      communityServiceProvider = communityServiceProvider,
    )
  }

  fun submitReferral(referralId: UUID, userId: UUID): SubmitReferralResponseDto {
    val referral = referralRepository.findById(referralId)
      .orElseThrow { NotFoundException("Referral not found for id $referralId") }

    val providerAssignment = referralProviderAssignmentRepository.findByReferralId(referralId)
      .firstOrNull() ?: throw NotFoundException("Provider assignment not found for referral id $referralId")

    val communityServiceProvider = providerAssignment.communityServiceProvider

    val referralEvent = ReferralEvent(
      id = UUID.randomUUID(),
      eventType = ReferralEventType.SUBMITTED,
      createdAt = OffsetDateTime.now(),
      actorType = ActorType.AUTH,
      actorId = userId,
      referral = referral,
    )

    referral.addEvent(referralEvent)
    referral.referenceNumber = generateReferenceNumber(communityServiceProvider, referralId)
    val savedReferral = referralRepository.save(referral)
    return SubmitReferralResponseDto(
      referralId = savedReferral.id,
      referenceNumber = savedReferral.referenceNumber,
    )
  }

  private fun generateReferenceNumber(communityServiceProvider: CommunityServiceProvider, referralId: UUID): String {
    val type = communityServiceProvider.serviceProvider.name

    for (i in 1..MAX_REFERENCE_NUMBER_TRIES) {
      val candidate = referenceGenerator.generate(type)
      if (!referralRepository.existsByReferenceNumber(candidate)) {
        return candidate
      } else {
        logger.warn("Clash found for referral number attempt {} for referral {}", i, referralId)
      }
    }

    logger.error("Unable to generate a unique referral number for referral : {}", referralId)
    throw IllegalStateException("Unable to generate a unique referral reference for referral $referralId")
  }

  private fun upsertPerson(personDetails: PersonDto): Person {
    val existing = personRepository.findByIdentifier(personDetails.personIdentifier!!) ?: return personRepository.save(
      personDetails.toEntity(),
    )
    val desiredGender = personDetails.sex ?: existing.gender
    val additionalDetailsChanged = !additionalDetailsEqual(existing.additionalDetails, personDetails.additionalDetails)
    val basicFieldsChanged = existing.firstName != personDetails.firstName ||
      existing.lastName != personDetails.lastName ||
      !existing.dateOfBirth.isEqual(personDetails.dateOfBirth) ||
      existing.gender != desiredGender

    if (!basicFieldsChanged && !additionalDetailsChanged) return existing

    val updatedPerson = Person(
      id = existing.id,
      identifier = existing.identifier,
      firstName = personDetails.firstName,
      lastName = personDetails.lastName,
      dateOfBirth = personDetails.dateOfBirth,
      gender = desiredGender,
      createdAt = existing.createdAt,
      updatedAt = OffsetDateTime.now(),
    )

    if (personDetails.additionalDetails != null) {
      updatedPerson.additionalDetails = PersonAdditionalDetails(
        id = existing.additionalDetails?.id ?: UUID.randomUUID(),
        person = updatedPerson,
        ethnicity = personDetails.additionalDetails.ethnicity,
        preferredLanguage = personDetails.additionalDetails.preferredLanguage,
        neurodiverseConditions = personDetails.additionalDetails.neurodiverseConditions,
        religionOrBelief = personDetails.additionalDetails.religionOrBelief,
        transgender = personDetails.additionalDetails.transgender,
        sexualOrientation = personDetails.additionalDetails.sexualOrientation,
        address = personDetails.additionalDetails.address,
        phoneNumber = personDetails.additionalDetails.phoneNumber,
        emailAddress = personDetails.additionalDetails.emailAddress,
      )
    } else {
      updatedPerson.additionalDetails = existing.additionalDetails
    }

    return personRepository.save(updatedPerson)
  }

  private fun additionalDetailsEqual(existing: PersonAdditionalDetails?, incoming: uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAdditionalDetails?): Boolean {
    if (incoming == null) return existing == null
    if (existing == null) return false
    return existing.ethnicity == incoming.ethnicity &&
      existing.preferredLanguage == incoming.preferredLanguage &&
      existing.neurodiverseConditions == incoming.neurodiverseConditions &&
      existing.religionOrBelief == incoming.religionOrBelief &&
      existing.transgender == incoming.transgender &&
      existing.sexualOrientation == incoming.sexualOrientation &&
      existing.address == incoming.address &&
      existing.phoneNumber == incoming.phoneNumber &&
      existing.emailAddress == incoming.emailAddress
  }
}
