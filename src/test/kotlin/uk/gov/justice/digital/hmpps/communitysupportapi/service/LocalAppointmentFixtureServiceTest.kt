package uk.gov.justice.digital.hmpps.communitysupportapi.service

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository

class LocalAppointmentFixtureServiceTest {
  private val personRepository = mock<PersonRepository>()
  private val referralRepository = mock<ReferralRepository>()
  private val referralUserRepository = mock<ReferralUserRepository>()
  private val localWireMockFixtureClient = mock<LocalWireMockFixtureClient>()
  private val service = LocalAppointmentFixtureService(
    personRepository,
    referralRepository,
    referralUserRepository,
    localWireMockFixtureClient,
    ReferralReferenceGenerator(),
  )

  @Test
  fun `creates submitted referrals with persisted person details and nDelius stubs`() {
    whenever(referralUserRepository.findByHmppsAuthUsernameIgnoreCase(any())).thenReturn(null)
    whenever(referralUserRepository.save(any<ReferralUser>())).thenAnswer { it.getArgument<ReferralUser>(0) }
    whenever(personRepository.findByIdentifier(any())).thenReturn(null)
    whenever(personRepository.save(any<Person>())).thenAnswer { it.getArgument<Person>(0) }
    whenever(referralRepository.existsByReferenceNumber(any())).thenReturn(false)
    whenever(referralRepository.saveAndFlush(any<Referral>())).thenAnswer { it.getArgument<Referral>(0) }

    val fixtures = service.createFixtures(2)

    fixtures.size shouldBe 2
    fixtures.map { it.caseReference }.all { it.matches(Regex("^[A-Z]{2}\\d{4}[A-Z]{2}$")) } shouldBe true
    fixtures.map { it.crn }.all { it.matches(Regex("^X\\d{6}$")) } shouldBe true

    val personCaptor = argumentCaptor<Person>()
    verify(personRepository, times(2)).save(personCaptor.capture())
    personCaptor.allValues.forEach { person ->
      person.additionalDetails?.person shouldBe person
      person.additionalDetails?.preferredLanguage shouldBe "English"
    }

    val referralCaptor = argumentCaptor<Referral>()
    verify(referralRepository, times(2)).saveAndFlush(referralCaptor.capture())
    referralCaptor.allValues.forEach { referral ->
      referral.submittedEvent?.eventType shouldBe ReferralEventType.SUBMITTED
      referral.referralEvents.mapNotNull { it.eventType } shouldBe listOf(
        ReferralEventType.CREATED,
        ReferralEventType.SUBMITTED,
      )
    }

    fixtures.forEach { fixture ->
      verify(localWireMockFixtureClient).registerPersonalDetailsFixture(fixture.crn)
    }
  }
}
