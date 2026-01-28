package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralCreationResult
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralDetailsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SubmitReferralResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActorType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ReferralService(
  private val referralRepository: ReferralRepository,
  private val personRepository: PersonRepository,
  private val communityServiceProviderRepository: CommunityServiceProviderRepository,
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

  fun createReferral(user: String, createReferralRequest: CreateReferralRequest): ReferralCreationResult {
    if (!personRepository.existsById(createReferralRequest.personId)) {
      throw NotFoundException("Person not found for id ${createReferralRequest.personId}")
    }

    val person = personRepository.findById(createReferralRequest.personId).get()
    val communityServiceProvider = communityServiceProviderRepository.findById(createReferralRequest.communityServiceProviderId)
      .orElseThrow { NotFoundException("Community Service Provider not found for id ${createReferralRequest.communityServiceProviderId}") }

    val referralId = UUID.randomUUID()
    val now = OffsetDateTime.now()

    val referral = Referral(
      id = referralId,
      crn = createReferralRequest.crn,
      personId = createReferralRequest.personId,
      communityServiceProviderId = createReferralRequest.communityServiceProviderId,
      createdAt = now,
      updatedAt = now,
      urgency = createReferralRequest.urgency,
    )

    val referralEvent = ReferralEvent(
      id = UUID.randomUUID(),
      eventType = ReferralEventType.CREATED,
      createdAt = now,
      actorType = ActorType.AUTH,
      actorId = user,
      referral = referral,
    )

    referral.addEvent(referralEvent)
    val savedReferral = referralRepository.save(referral)
    return ReferralCreationResult(
      referral = savedReferral,
      person = person,
      communityServiceProvider = communityServiceProvider,
    )
  }

  fun submitReferral(referralId: UUID, user: String): SubmitReferralResponseDto {
    val referral = referralRepository.findById(referralId)
      .orElseThrow { NotFoundException("Referral not found for id $referralId") }

    val communityServiceProvider = communityServiceProviderRepository.findById(referral.communityServiceProviderId)
      .orElseThrow { NotFoundException("Community Service Provider not found for id ${referral.communityServiceProviderId}") }

    val referralEvent = ReferralEvent(
      id = UUID.randomUUID(),
      eventType = ReferralEventType.SUBMITTED,
      createdAt = OffsetDateTime.now(),
      actorType = ActorType.AUTH,
      actorId = user,
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
    val type = communityServiceProvider.providerName

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
}
