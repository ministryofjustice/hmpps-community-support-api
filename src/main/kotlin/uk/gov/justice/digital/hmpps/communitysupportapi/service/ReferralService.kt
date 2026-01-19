package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralCreationResult
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.time.LocalDateTime
import java.util.UUID

@Service
class ReferralService(
  private val referralRepository: ReferralRepository,
  private val personRepository: PersonRepository,
  private val communityServiceProviderRepository: CommunityServiceProviderRepository,
  private val referralEventRepository: ReferralEventRepository,
  private val referenceGenerator: ReferralReferenceGenerator,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(ReferralService::class.java)
    private const val MAX_REFERENCE_NUMBER_TRIES = 10
  }

  fun getReferral(referralId: UUID) = referralRepository.findById(referralId)

  fun createReferral(user: String, createReferralRequest: CreateReferralRequest): ReferralCreationResult {
    if (!personRepository.existsById(createReferralRequest.personId)) {
      throw NotFoundException("Person not found for id ${createReferralRequest.personId}")
    }

    val person = personRepository.findById(createReferralRequest.personId).get()
    val communityServiceProvider = communityServiceProviderRepository.findById(createReferralRequest.communityServiceProviderId)
      .orElseThrow { NotFoundException("Community Service Provider not found for id ${createReferralRequest.communityServiceProviderId}") }

    val referralId = UUID.randomUUID()
    val now = LocalDateTime.now()

    val referenceNumber = generateReferenceNumberOrThrow(communityServiceProvider, referralId)

    val referral = Referral(
      id = UUID.randomUUID(),
      crn = createReferralRequest.crn,
      personId = createReferralRequest.personId,
      referenceNumber = referenceNumber,
      communityServiceProviderId = createReferralRequest.communityServiceProviderId,
      createdAt = now,
      updatedAt = now,
      urgency = createReferralRequest.urgency,
    )

    logger.info("Created referral for crn = {}, referenceNumber = {}", referral.crn, referral.referenceNumber)

    val referralEvent = ReferralEvent(
      id = UUID.randomUUID(),
      eventType = ReferralEventType.SUBMITTED.value,
      createdAt = now,
      actorType = ActorType.AUTH.value,
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

  private fun generateReferenceNumberOrThrow(communityServiceProvider: CommunityServiceProvider, referralId: UUID): String {
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

enum class ReferralEventType(val value: String) {
  SUBMITTED("SUBMITTED"),
  UPDATED("UPDATED"),
}

enum class ActorType(val value: String) {
  AUTH("AUTH"),
  EXTERNAL("EXTERNAL"),
}
