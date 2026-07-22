package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.client.AssessRisksAndNeedsClient
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CommunitySupportRiskInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.arns.ArnsRiskConcernsToSelfDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.arns.ArnsRiskRoshSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.arns.CommunitySupportRiskDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.RiskInformation
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.RiskInformationRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedAssessmentDate
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedDateOfBirthLong
import uk.gov.justice.digital.hmpps.communitysupportapi.validation.PersonIdentifierValidator
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

@Service
class RiskInformationService(
  private val assessRisksAndNeedsClient: AssessRisksAndNeedsClient,
  private val riskInformationRepository: RiskInformationRepository,
  private val referralRepository: ReferralRepository,
  private val personRepository: PersonRepository,
  private val identifierValidator: PersonIdentifierValidator,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getRoshRisksByReferralId(referralId: UUID): CommunitySupportRiskDto {
    val referral = referralRepository.findById(referralId)
      .orElseThrow { NotFoundException("Referral not found for id $referralId") }

    val person = personRepository.findById(referral.personId)
      .orElseThrow { NotFoundException("Person not found for referral $referralId") }

    val firstName = person.firstName
    val lastName = person.lastName
    val dateOfBirth = person.dateOfBirth.toFormattedDateOfBirthLong()

    val identifier = identifierValidator.validate(person.identifier)
    if (identifier !is PersonIdentifier.Crn) {
      log.info("Skipping ROSH risk lookup for referral {} as person identifier is not a CRN", referralId)
      return buildCommunitySupportRiskDto(firstName, lastName, dateOfBirth)
    }

    val crn = identifier.value

    log.info("Fetching ROSH risks from Assess Risks and Needs for referral {} (CRN: {})", referralId, crn)
    val arnsResponse = assessRisksAndNeedsClient.getRoshRisksByCrn(crn)

    val withinTwelveMonths = arnsResponse.assessedOn
      ?.isAfter(LocalDateTime.now().minusMonths(12))
      ?: false

    log.info("Risk assessment for CRN {} within 12 months: {}", crn, withinTwelveMonths)

    return if (withinTwelveMonths) {
      buildCommunitySupportRiskDto(
        firstName = firstName,
        lastName = lastName,
        crn = crn,
        dateOfBirth = dateOfBirth,
        assessmentWithin12Months = true,
        assessedOn = arnsResponse.assessedOn.toFormattedAssessmentDate(),
        riskToSelf = arnsResponse.riskToSelf,
        summary = arnsResponse.summary,
      )
    } else {
      buildCommunitySupportRiskDto(
        firstName = firstName,
        lastName = lastName,
        crn = crn,
        dateOfBirth = dateOfBirth,
        assessmentWithin12Months = false,
      )
    }
  }

  private fun buildCommunitySupportRiskDto(
    firstName: String,
    lastName: String,
    dateOfBirth: String,
    crn: String = "",
    assessmentWithin12Months: Boolean = false,
    assessedOn: String? = null,
    riskToSelf: ArnsRiskConcernsToSelfDto? = null,
    summary: ArnsRiskRoshSummaryDto? = null,
  ): CommunitySupportRiskDto = CommunitySupportRiskDto(
    firstName = firstName,
    lastName = lastName,
    crn = crn,
    dateOfBirth = dateOfBirth,
    assessmentWithin12Months = assessmentWithin12Months,
    assessedOn = assessedOn,
    riskToSelf = riskToSelf,
    summary = summary,
  )

  @Transactional
  fun saveDraftRiskInformation(
    referralId: UUID,
    userId: UUID,
    request: CommunitySupportRiskInformationDto,
  ): CommunitySupportRiskInformationDto {
    if (!referralRepository.existsById(referralId)) {
      throw NotFoundException("Referral not found for id $referralId")
    }

    val existing = riskInformationRepository.findByReferralId(referralId)
    val referral = referralRepository.findById(referralId).get()

    val riskInformation = existing?.apply {
      riskSummaryWhoIsAtRisk = request.riskSummaryWhoIsAtRisk
      riskSummaryNatureOfRisk = request.riskSummaryNatureOfRisk
      riskSummaryRiskImminence = request.riskSummaryRiskImminence
      riskToSelfSuicide = request.riskToSelfSuicide
      riskToSelfHarm = request.riskToSelfSelfHarm
      riskToSelfHostelSetting = request.riskToSelfHostelSetting
      riskToSelfVulnerability = request.riskToSelfVulnerability
      additionalInformation = request.additionalInformation
      updatedAt = OffsetDateTime.now()
      updatedBy = userId
    } ?: RiskInformation(
      id = UUID.randomUUID(),
      referralId = referralId,
      riskSummaryWhoIsAtRisk = request.riskSummaryWhoIsAtRisk,
      riskSummaryNatureOfRisk = request.riskSummaryNatureOfRisk,
      riskSummaryRiskImminence = request.riskSummaryRiskImminence,
      riskToSelfSuicide = request.riskToSelfSuicide,
      riskToSelfHarm = request.riskToSelfSelfHarm,
      riskToSelfHostelSetting = request.riskToSelfHostelSetting,
      riskToSelfVulnerability = request.riskToSelfVulnerability,
      additionalInformation = request.additionalInformation,
      updatedAt = OffsetDateTime.now(),
      updatedBy = userId,
      referral = referral,
    )

    val saved = riskInformationRepository.save(riskInformation)
    return CommunitySupportRiskInformationDto.from(saved)
  }
}
