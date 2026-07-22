package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.communitysupportapi.client.AssessRisksAndNeedsClient
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CommunitySupportRiskInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.RiskInformation
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.RiskInformationRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createArnsRoshRiskDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createStaleArnsRoshRiskDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedAssessmentDate
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedDateOfBirthLong
import uk.gov.justice.digital.hmpps.communitysupportapi.validation.PersonIdentifierValidator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class RiskInformationServiceTest {

  @Mock
  lateinit var assessRisksAndNeedsClient: AssessRisksAndNeedsClient

  @Mock
  lateinit var riskInformationRepository: RiskInformationRepository

  @Mock
  lateinit var referralRepository: ReferralRepository

  @Mock
  lateinit var personRepository: PersonRepository

  @Mock
  lateinit var identifierValidator: PersonIdentifierValidator

  @InjectMocks
  lateinit var riskInformationService: RiskInformationService

  private val referralId = UUID.randomUUID()
  private val userId = UUID.randomUUID()
  private val personId = UUID.randomUUID()

  private val person = PersonFactory()
    .withId(personId)
    .withIdentifier(CRN)
    .withFirstName("John")
    .withLastName("Smith")
    .withDateOfBirth(LocalDate.of(1988, 1, 1))
    .create()

  private fun stubPersonLookup(referral: Referral) {
    whenever(referralRepository.findById(referralId)).thenReturn(Optional.of(referral))
    whenever(personRepository.findById(referral.personId)).thenReturn(Optional.of(person))
    whenever(identifierValidator.validate(person.identifier)).thenReturn(PersonIdentifier.Crn(CRN))
  }

  @Test
  fun `should return full risk data when assessment is within 12 months`() {
    val referral = ReferralFactory().withId(referralId).withCrn(CRN).withPersonId(personId).create()
    stubPersonLookup(referral)

    val recentAssessment = createArnsRoshRiskDto(assessedOn = LocalDateTime.now().minusDays(30))
    whenever(assessRisksAndNeedsClient.getRoshRisksByCrn(CRN)).thenReturn(recentAssessment)

    val result = riskInformationService.getRoshRisksByReferralId(referralId)

    assertTrue(result.assessmentWithin12Months)
    assertNotNull(result.riskToSelf)
    assertNotNull(result.summary)
    assertEquals(recentAssessment.assessedOn?.toFormattedAssessmentDate(), result.assessedOn)
    assertEquals("HIGH", result.summary?.overallRiskLevel)
    assertEquals("John", result.firstName)
    assertEquals("Smith", result.lastName)
    assertEquals(CRN, result.crn)
    assertEquals(LocalDate.of(1988, 1, 1).toFormattedDateOfBirthLong(), result.dateOfBirth)
    verify(assessRisksAndNeedsClient).getRoshRisksByCrn(CRN)
  }

  @Test
  fun `should return blank risk data when assessment is older than 12 months`() {
    val referral = ReferralFactory().withId(referralId).withCrn(CRN).withPersonId(personId).create()
    stubPersonLookup(referral)

    val staleAssessment = createStaleArnsRoshRiskDto()
    whenever(assessRisksAndNeedsClient.getRoshRisksByCrn(CRN)).thenReturn(staleAssessment)

    val result = riskInformationService.getRoshRisksByReferralId(referralId)

    assertFalse(result.assessmentWithin12Months)
    assertNull(result.riskToSelf)
    assertNull(result.summary)
    assertNull(result.assessedOn)
    assertEquals("John", result.firstName)
    assertEquals("Smith", result.lastName)
    assertEquals(CRN, result.crn)
    verify(assessRisksAndNeedsClient).getRoshRisksByCrn(CRN)
  }

  @Test
  fun `should return blank risk data when assessedOn is null`() {
    val referral = ReferralFactory().withId(referralId).withCrn(CRN).withPersonId(personId).create()
    stubPersonLookup(referral)

    val noDateAssessment = createArnsRoshRiskDto(assessedOn = null)
    whenever(assessRisksAndNeedsClient.getRoshRisksByCrn(CRN)).thenReturn(noDateAssessment)

    val result = riskInformationService.getRoshRisksByReferralId(referralId)

    assertFalse(result.assessmentWithin12Months)
    assertNull(result.riskToSelf)
    assertNull(result.summary)
    verify(assessRisksAndNeedsClient).getRoshRisksByCrn(CRN)
  }

  @Test
  fun `should propagate NotFoundException when CRN is not found`() {
    val referral = ReferralFactory().withId(referralId).withCrn(CRN).withPersonId(personId).create()
    stubPersonLookup(referral)

    whenever(assessRisksAndNeedsClient.getRoshRisksByCrn(CRN))
      .thenThrow(NotFoundException("ROSH risks not found in Assess Risks and Needs for CRN: $CRN"))

    assertThrows<NotFoundException> {
      riskInformationService.getRoshRisksByReferralId(referralId)
    }

    verify(assessRisksAndNeedsClient).getRoshRisksByCrn(CRN)
  }

  @Test
  fun `should not call Assess Risks and Needs when person identifier is not a CRN`() {
    val prisonerPerson = PersonFactory()
      .withId(personId)
      .withIdentifier(PRISONER_NUMBER)
      .withFirstName("John")
      .withLastName("Smith")
      .withDateOfBirth(LocalDate.of(1988, 1, 1))
      .create()

    val referral = ReferralFactory().withId(referralId).withCrn(PRISONER_NUMBER).withPersonId(personId).create()

    whenever(referralRepository.findById(referralId)).thenReturn(Optional.of(referral))
    whenever(personRepository.findById(referral.personId)).thenReturn(Optional.of(prisonerPerson))
    whenever(identifierValidator.validate(PRISONER_NUMBER)).thenReturn(PersonIdentifier.PrisonerNumber(PRISONER_NUMBER))

    val result = riskInformationService.getRoshRisksByReferralId(referralId)

    assertFalse(result.assessmentWithin12Months)
    assertNull(result.riskToSelf)
    assertNull(result.summary)
    assertNull(result.assessedOn)
    assertEquals("John", result.firstName)
    assertEquals("Smith", result.lastName)
    assertEquals("", result.crn)
    assertEquals(LocalDate.of(1988, 1, 1).toFormattedDateOfBirthLong(), result.dateOfBirth)
    verifyNoInteractions(assessRisksAndNeedsClient)
  }

  @Test
  fun `should throw NotFoundException when referral does not exist for getRoshRisksByReferralId`() {
    whenever(referralRepository.findById(referralId)).thenReturn(Optional.empty())

    assertThrows<NotFoundException> {
      riskInformationService.getRoshRisksByReferralId(referralId)
    }

    verify(referralRepository).findById(referralId)
  }

  @Test
  fun `should throw NotFoundException when referral does not exist`() {
    whenever(referralRepository.existsById(referralId)).thenReturn(false)

    assertThrows<NotFoundException> {
      riskInformationService.saveDraftRiskInformation(
        referralId = referralId,
        userId = userId,
        request = CommunitySupportRiskInformationDto(id = UUID.randomUUID(), referralId = referralId),
      )
    }

    verify(referralRepository).existsById(referralId)
  }

  @Test
  fun `should create new draft risk information when none exists for the referral`() {
    val referral = ReferralFactory().withId(referralId).create()
    whenever(referralRepository.existsById(referralId)).thenReturn(true)
    whenever(referralRepository.findById(referralId)).thenReturn(Optional.of(referral))
    whenever(riskInformationRepository.findByReferralId(referralId)).thenReturn(null)
    whenever(riskInformationRepository.save(any<RiskInformation>())).thenAnswer { it.arguments[0] as RiskInformation }

    val request = CommunitySupportRiskInformationDto(
      id = UUID.randomUUID(),
      referralId = referralId,
      riskSummaryWhoIsAtRisk = "Staff and public",
      riskSummaryNatureOfRisk = "Physical harm",
      riskSummaryRiskImminence = "Low",
      riskToSelfSuicide = "No current concerns",
      riskToSelfSelfHarm = "No current concerns",
      riskToSelfHostelSetting = "Not applicable",
      riskToSelfVulnerability = "None identified",
      additionalInformation = "Some additional notes",
    )

    val result = riskInformationService.saveDraftRiskInformation(referralId, userId, request)

    val captor = ArgumentCaptor.forClass(RiskInformation::class.java)
    verify(riskInformationRepository).save(captor.capture())
    val saved = captor.value

    assertNotNull(saved.id)
    assertEquals(referralId, saved.referralId)
    assertEquals(userId, saved.updatedBy)
    assertNotNull(saved.updatedAt)
    assertEquals("Staff and public", saved.riskSummaryWhoIsAtRisk)
    assertEquals("Physical harm", saved.riskSummaryNatureOfRisk)
    assertEquals("Low", saved.riskSummaryRiskImminence)
    assertEquals("No current concerns", saved.riskToSelfSuicide)
    assertEquals("No current concerns", saved.riskToSelfHarm)
    assertEquals("Not applicable", saved.riskToSelfHostelSetting)
    assertEquals("None identified", saved.riskToSelfVulnerability)
    assertEquals("Some additional notes", saved.additionalInformation)

    assertEquals(CommunitySupportRiskInformationDto.from(saved), result)
  }

  @Test
  fun `should update existing draft risk information for the referral`() {
    val existingId = UUID.randomUUID()
    val originalUpdatedBy = UUID.randomUUID()
    val originalUpdatedAt = OffsetDateTime.now().minusDays(2)
    val referral = ReferralFactory().withId(referralId).create()

    val existing = RiskInformation(
      id = existingId,
      referralId = referralId,
      riskSummaryWhoIsAtRisk = "Old summary",
      updatedAt = originalUpdatedAt,
      updatedBy = originalUpdatedBy,
      referral = referral,
    )

    whenever(referralRepository.existsById(referralId)).thenReturn(true)
    whenever(referralRepository.findById(referralId)).thenReturn(Optional.of(referral))
    whenever(riskInformationRepository.findByReferralId(referralId)).thenReturn(existing)
    whenever(riskInformationRepository.save(any<RiskInformation>())).thenAnswer { it.arguments[0] as RiskInformation }

    val request = CommunitySupportRiskInformationDto(
      id = UUID.randomUUID(),
      referralId = referralId,
      riskSummaryWhoIsAtRisk = "Updated summary",
    )

    val result = riskInformationService.saveDraftRiskInformation(referralId, userId, request)

    val captor = ArgumentCaptor.forClass(RiskInformation::class.java)
    verify(riskInformationRepository).save(captor.capture())
    val saved = captor.value

    assertEquals(existingId, saved.id)
    assertEquals(referralId, saved.referralId)
    assertEquals(userId, saved.updatedBy)
    assertNotEquals(originalUpdatedAt, saved.updatedAt)
    assertEquals("Updated summary", saved.riskSummaryWhoIsAtRisk)

    assertEquals(CommunitySupportRiskInformationDto.from(saved), result)
  }
}
