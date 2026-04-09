package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralAppointmentHistoryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralCreationResult
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralDetailsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralProgressDto
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
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CaseIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentStatusHistoryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.validation.CaseIdentifierValidator
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

@Service
class ReferralService(
  private val referralRepository: ReferralRepository,
  private val personRepository: PersonRepository,
  private val appointmentRepository: AppointmentRepository,
  private val appointmentIcsRepository: AppointmentIcsRepository,
  private val appointmentStatusHistoryRepository: AppointmentStatusHistoryRepository,
  private val communityServiceProviderRepository: CommunityServiceProviderRepository,
  private val referralProviderAssignmentRepository: ReferralProviderAssignmentRepository,
  private val referralUserAssignmentRepository: ReferralUserAssignmentRepository,
  private val referenceGenerator: ReferralReferenceGenerator,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(ReferralService::class.java)
    private val identifierValidator: CaseIdentifierValidator = CaseIdentifierValidator()
    private const val MAX_REFERENCE_NUMBER_TRIES = 10
  }

  fun getReferral(referralId: UUID) = referralRepository.findById(referralId)

  fun getReferralDetailsPage(caseIdentifier: String?): ReferralDetailsBffResponseDto {
    val foundReferral = when (val identifier = identifierValidator.validate(caseIdentifier)) {
      is CaseIdentifier.ReferralId -> referralRepository.findById(identifier.value)
        .orElseThrow { NotFoundException("Referral not found for id $identifier.value") }

      is CaseIdentifier.CaseId -> referralRepository.findByReferenceNumber(identifier.value)
        .firstOrNull() ?: throw NotFoundException("Referral not found for reference ${identifier.value}")
    }
    val person = personRepository.findById(foundReferral.personId).orElseThrow { NotFoundException("Person not found for referral ${foundReferral?.personId}") }
    val referralAssignments = referralUserAssignmentRepository.findAllByReferralIdAndNotDeleted(foundReferral.id)

    return ReferralDetailsBffResponseDto.from(foundReferral, person, referralAssignments)
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

  fun getReferralProgress(referralIdentifier: String): ReferralProgressDto {
    val referral = when (val identifier = identifierValidator.validate(referralIdentifier)) {
      is CaseIdentifier.ReferralId -> referralRepository.findById(identifier.value)
        .orElseThrow { NotFoundException("Referral not found for id $identifier.value") }

      is CaseIdentifier.CaseId -> referralRepository.findByReferenceNumber(identifier.value).first()
    }

    val personName = personRepository.findById(referral.personId)
      .orElseThrow { NotFoundException("Person not found for referral $referralIdentifier") }
      .let { "${it.firstName} ${it.lastName}" }

    val appointments = appointmentRepository.findAllByReferralId(referral.id).orEmpty()

    if (appointments.isEmpty()) {
      return ReferralProgressDto(referralId = referral.id, fullName = personName, appointments = emptyList())
    }

    val appointmentIds = appointments.map { it.id }

    val icsByAppointment = appointmentIcsRepository
      .findAllByAppointmentIdIn(appointmentIds)
      .associateBy { it.appointment.id }

    check(appointmentIds.all { it in icsByAppointment }) {
      "Missing ICS for appointments: ${appointmentIds - icsByAppointment.keys}"
    }

    val statusHistoryByAppointment = appointmentStatusHistoryRepository
      .findAllByAppointmentIdIn(appointmentIds)
      .groupBy { it.appointment.id }

    val appointmentHistory = appointments.map { appointment ->
      val ics = icsByAppointment.getValue(appointment.id)

      val latestStatus = statusHistoryByAppointment[appointment.id]
        ?.maxByOrNull { it.createdAt }
        ?.status
        ?: error("No status history for appointment ${appointment.id}")

      ReferralAppointmentHistoryDto(
        appointmentId = appointment.id,
        type = appointment.type,
        dateTime = ics.appointmentDateTime,
        status = latestStatus,
      )
    }

    return ReferralProgressDto(referralId = referral.id, fullName = personName, appointments = appointmentHistory)
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
