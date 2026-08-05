package uk.gov.justice.digital.hmpps.communitysupportapi.service

import jakarta.validation.ValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralCriminogenicNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CriminogenicNeedsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralCriminogenicNeedsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralFactory
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class CriminogenicNeedsServiceTest {

  @Mock
  lateinit var referralRepository: ReferralRepository

  @Mock
  lateinit var personRepository: PersonRepository

  @Mock
  lateinit var referralCriminogenicNeedsRepository: ReferralCriminogenicNeedsRepository

  @InjectMocks
  lateinit var criminogenicNeedsService: CriminogenicNeedsService

  private val referralId = UUID.randomUUID()
  private val userId = UUID.randomUUID()

  @Test
  fun `getCriminogenicNeeds throws NotFoundException when referral does not exist`() {
    whenever(referralRepository.findById(referralId)).thenReturn(Optional.empty())

    assertThrows<NotFoundException> { criminogenicNeedsService.getCriminogenicNeeds(referralId) }

    verify(referralRepository).findById(referralId)
  }

  @Test
  fun `getCriminogenicNeeds returns dto when criminogenic needs exist`() {
    val referral = ReferralFactory().withId(referralId).create()
    val person = PersonFactory().withId(referral.personId).withFirstName("Alex").withLastName("River").create()
    val needs = ReferralCriminogenicNeeds(
      id = UUID.randomUUID(),
      referral = referral,
      hasAccommodationNeeds = true,
      accommodationDetails = "Needs stable accommodation",
      updatedAt = OffsetDateTime.now(),
      updatedBy = userId,
    )

    whenever(referralRepository.findById(referralId)).thenReturn(Optional.of(referral))
    whenever(personRepository.findById(referral.personId)).thenReturn(Optional.of(person))
    whenever(referralCriminogenicNeedsRepository.findByReferralId(referralId)).thenReturn(needs)

    val result = criminogenicNeedsService.getCriminogenicNeeds(referralId)

    assertEquals(needs.id, result.id)
    assertEquals(referral.id, result.referralId)
    assertEquals("Alex", result.refereeName.firstName)
    assertEquals("River", result.refereeName.lastName)
    assertEquals(true, result.hasAccommodationNeeds)
    assertEquals("Needs stable accommodation", result.accommodationDetails)
    assertEquals(userId, result.updatedBy)
  }

  @Test
  fun `upsertCriminogenicNeeds creates new record when none exists`() {
    val referral = ReferralFactory().withId(referralId).create()
    val person = PersonFactory().withId(referral.personId).withFirstName("Alex").withLastName("River").create()
    val request = CriminogenicNeedsRequest(
      hasAccommodationNeeds = true,
      accommodationDetails = "Requires temporary housing",
      hasDrugUseNeeds = false,
      hasAlcoholUseNeeds = true,
      alcoholUseDetails = "Needs alcohol support",
    )

    whenever(referralRepository.findById(referralId)).thenReturn(Optional.of(referral))
    whenever(personRepository.findById(referral.personId)).thenReturn(Optional.of(person))
    whenever(referralCriminogenicNeedsRepository.findByReferralId(referralId)).thenReturn(null)
    whenever(referralCriminogenicNeedsRepository.save(any<ReferralCriminogenicNeeds>()))
      .thenAnswer { it.arguments[0] as ReferralCriminogenicNeeds }

    val result = criminogenicNeedsService.upsertCriminogenicNeeds(referralId, userId, request)

    val captor = ArgumentCaptor.forClass(ReferralCriminogenicNeeds::class.java)
    verify(referralCriminogenicNeedsRepository).save(captor.capture())
    val saved = captor.value

    assertNotNull(saved.id)
    assertEquals(referral.id, saved.referral.id)
    assertEquals(true, saved.hasAccommodationNeeds)
    assertEquals("Requires temporary housing", saved.accommodationDetails)
    assertEquals(false, saved.hasDrugUseNeeds)
    assertEquals(true, saved.hasAlcoholUseNeeds)
    assertEquals("Needs alcohol support", saved.alcoholUseDetails)
    assertEquals(userId, saved.updatedBy)
    assertNotNull(saved.updatedAt)

    assertEquals(saved.id, result.id)
    assertEquals(saved.referral.id, result.referralId)
    assertEquals("Alex", result.refereeName.firstName)
    assertEquals("River", result.refereeName.lastName)
  }

  @Test
  fun `upsertCriminogenicNeeds updates existing record and preserves id`() {
    val referral = ReferralFactory().withId(referralId).create()
    val person = PersonFactory().withId(referral.personId).withFirstName("Alex").withLastName("River").create()
    val existingId = UUID.randomUUID()
    val originalUpdatedAt = OffsetDateTime.now().minusDays(1)

    val existing = ReferralCriminogenicNeeds(
      id = existingId,
      referral = referral,
      hasAccommodationNeeds = false,
      updatedAt = originalUpdatedAt,
      updatedBy = UUID.randomUUID(),
    )

    val request = CriminogenicNeedsRequest(hasAccommodationNeeds = true, accommodationDetails = "Updated accommodation details")

    whenever(referralRepository.findById(referralId)).thenReturn(Optional.of(referral))
    whenever(personRepository.findById(referral.personId)).thenReturn(Optional.of(person))
    whenever(referralCriminogenicNeedsRepository.findByReferralId(referralId)).thenReturn(existing)
    whenever(referralCriminogenicNeedsRepository.save(any<ReferralCriminogenicNeeds>()))
      .thenAnswer { it.arguments[0] as ReferralCriminogenicNeeds }

    val result = criminogenicNeedsService.upsertCriminogenicNeeds(referralId, userId, request)

    val captor = ArgumentCaptor.forClass(ReferralCriminogenicNeeds::class.java)
    verify(referralCriminogenicNeedsRepository).save(captor.capture())
    val saved = captor.value

    assertEquals(existingId, saved.id)
    assertEquals(referral.id, saved.referral.id)
    assertEquals(true, saved.hasAccommodationNeeds)
    assertEquals("Updated accommodation details", saved.accommodationDetails)
    assertEquals(userId, saved.updatedBy)
    assertNotEquals(originalUpdatedAt, saved.updatedAt)

    assertEquals(existingId, result.id)
    assertEquals(referral.id, result.referralId)
    assertEquals("Alex", result.refereeName.firstName)
    assertEquals("River", result.refereeName.lastName)
  }

  @Test
  fun `upsertCriminogenicNeeds throws ValidationException when details are missing for selected need`() {
    val request = CriminogenicNeedsRequest(hasAccommodationNeeds = true, accommodationDetails = null)

    assertThrows<ValidationException> { criminogenicNeedsService.upsertCriminogenicNeeds(referralId, userId, request) }

    verify(referralRepository, never()).findById(referralId)
    verify(referralCriminogenicNeedsRepository, never()).save(any<ReferralCriminogenicNeeds>())
  }

  @Test
  fun `upsertCriminogenicNeeds clears details when corresponding boolean is false`() {
    val referral = ReferralFactory().withId(referralId).create()
    val person = PersonFactory().withId(referral.personId).create()
    val request = CriminogenicNeedsRequest(hasAccommodationNeeds = false, accommodationDetails = "This should be removed")

    whenever(referralRepository.findById(referralId)).thenReturn(Optional.of(referral))
    whenever(personRepository.findById(referral.personId)).thenReturn(Optional.of(person))
    whenever(referralCriminogenicNeedsRepository.findByReferralId(referralId)).thenReturn(null)
    whenever(referralCriminogenicNeedsRepository.save(any<ReferralCriminogenicNeeds>()))
      .thenAnswer { it.arguments[0] as ReferralCriminogenicNeeds }

    criminogenicNeedsService.upsertCriminogenicNeeds(referralId, userId, request)

    val captor = ArgumentCaptor.forClass(ReferralCriminogenicNeeds::class.java)
    verify(referralCriminogenicNeedsRepository).save(captor.capture())
    val saved = captor.value

    assertEquals(false, saved.hasAccommodationNeeds)
    assertEquals(null, saved.accommodationDetails)
  }
}
