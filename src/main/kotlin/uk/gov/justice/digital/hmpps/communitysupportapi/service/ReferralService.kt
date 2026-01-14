package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CheckReferralInformationRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@Service
@Transactional
class ReferralService(
  private val referralRepository: ReferralRepository,
  private val personRepository: PersonRepository,
  private val communityServiceProviderRepository: CommunityServiceProviderRepository,
  private val referralEventRepository: ReferralEventRepository,
  private val referenceGenerator: ReferralReferenceGenerator,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private const val MAX_REFERENCE_NUMBER_TRIES = 10
  }

  fun getReferral(referralId: UUID): Optional<Referral> = referralRepository.findById(referralId)

  fun createReferral(user: String, createReferralRequest: CreateReferralRequest): Referral {
    val communityServiceProvider = communityServiceProviderRepository.findById(createReferralRequest.communityServiceProviderId)
      .orElseThrow { NotFoundException("Community Service Provider not found for id ${createReferralRequest.communityServiceProviderId}") }

    val referralId = UUID.randomUUID()

    val referral = Referral(
      id = referralId,
      crn = createReferralRequest.crn,
      personId = createReferralRequest.personId,
      communityServiceProviderId = createReferralRequest.communityServiceProviderId,
      referenceNumber = generateReferenceNumber(communityServiceProvider, referralId),
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now(),
      urgency = createReferralRequest.urgency,
    )

    // save referral once to make it managed
    val savedReferral = referralRepository.save(referral)

    val referralEvent = ReferralEvent(
      id = UUID.randomUUID(),
      referral = savedReferral,
      eventType = "SUBMITTED",
      createdAt = LocalDateTime.now(),
      actorType = "auth",
      actorId = user,
    )

    referralEventRepository.save(referralEvent)
    savedReferral.referralEvents.add(referralEvent)
    return referralRepository.save(savedReferral)
  }

  fun checkReferralInformation(checkReferralInformationRequest: CheckReferralInformationRequest): ReferralInformationDto {
    val person = personRepository.findById(checkReferralInformationRequest.personId).orElseThrow {
      NotFoundException("Person not found for id ${checkReferralInformationRequest.personId}")
    }

    val communityServiceProvider = communityServiceProviderRepository.findById(checkReferralInformationRequest.communityServiceProviderId)
      .orElseThrow { NotFoundException("Community Service Provider not found for id ${checkReferralInformationRequest.communityServiceProviderId}") }

    return ReferralInformationDto(
      personId = checkReferralInformationRequest.personId,
      communityServiceProviderId = checkReferralInformationRequest.communityServiceProviderId,
      firstName = person.firstName,
      lastName = person.lastName,
      sex = person.sex,
      crn = checkReferralInformationRequest.crn,
      communityServiceProviderName = communityServiceProvider.name,
      region = communityServiceProvider.contractArea.region.name,
      deliveryPartner = communityServiceProvider.providerName,
    )
  }

  private fun generateReferenceNumber(communityServiceProvider: CommunityServiceProvider, referralId: UUID): String? {
    val type = communityServiceProvider.providerName

    for (i in 1..MAX_REFERENCE_NUMBER_TRIES) {
      val candidate = referenceGenerator.generate(type)
      if (!referralRepository.existsByReferenceNumber(candidate)) {
        return candidate
      } else {
        logger.warn(
          "Clash found for referral number {}",
          referralId,
        )
      }
    }
    logger.error(
      "Unable to generate a referral number for referral : {}",
      referralId,
    )
    return null
  }
}
