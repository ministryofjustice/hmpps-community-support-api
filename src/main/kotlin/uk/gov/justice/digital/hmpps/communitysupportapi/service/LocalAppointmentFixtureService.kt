package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActorType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

@Service
@Profile("local")
class LocalAppointmentFixtureService(
  private val personRepository: PersonRepository,
  private val referralRepository: ReferralRepository,
  private val referralUserRepository: ReferralUserRepository,
  private val localWireMockFixtureClient: LocalWireMockFixtureClient,
  private val referralReferenceGenerator: ReferralReferenceGenerator,
) {
  companion object {
    private const val FIXTURE_USERNAME = "local-appointment-fixtures"
    private val random = SecureRandom()
  }

  @Transactional
  fun createFixtures(count: Int): List<LocalAppointmentFixture> {
    val fixtureUser = findOrCreateFixtureUser()

    return (1..count).map { index ->
      val now = OffsetDateTime.now()
      val crn = generateCrn()
      val caseReference = generateCaseReference()
      val person = personRepository.save(
        Person(
          id = UUID.randomUUID(),
          identifier = crn,
          firstName = "Appointment$index",
          lastName = "Fixture",
          dateOfBirth = LocalDate.of(1985, 1, 1),
          gender = "Male",
          createdAt = now,
          updatedAt = now,
        ).also {
          it.additionalDetails = PersonAdditionalDetails(
            id = UUID.randomUUID(),
            person = it,
            ethnicity = "White British",
            preferredLanguage = "English",
            neurodiverseConditions = "None known",
            religionOrBelief = "None",
            address = "1 Fixture Street, London, SW1A 1AA",
            phoneNumber = "07123456789",
            emailAddress = "appointment.fixture$index@example.com",
          )
        },
      )

      val referral = referralRepository.saveAndFlush(
        Referral(
          id = UUID.randomUUID(),
          personId = person.id,
          personIdentifier = crn,
          referenceNumber = caseReference,
          createdAt = now,
          updatedAt = now,
          urgency = false,
          createdBy = fixtureUser.id,
        ).also {
          it.addEvent(
            ReferralEvent(
              id = UUID.randomUUID(),
              referral = it,
              eventType = ReferralEventType.CREATED,
              createdAt = now,
              actorType = ActorType.AUTH,
              actorId = fixtureUser.id,
            ),
          )
          it.addEvent(
            ReferralEvent(
              id = UUID.randomUUID(),
              referral = it,
              eventType = ReferralEventType.SUBMITTED,
              createdAt = now.plusNanos(1_000),
              actorType = ActorType.AUTH,
              actorId = fixtureUser.id,
            ),
          )
        },
      )

      localWireMockFixtureClient.registerPersonalDetailsFixture(crn)

      LocalAppointmentFixture(
        caseReference = caseReference,
        referralId = referral.id,
        crn = crn,
      )
    }
  }

  private fun findOrCreateFixtureUser(): ReferralUser = referralUserRepository.findByHmppsAuthUsernameIgnoreCase(FIXTURE_USERNAME)
    ?: referralUserRepository.save(
      ReferralUser(
        hmppsAuthId = FIXTURE_USERNAME,
        hmppsAuthUsername = FIXTURE_USERNAME,
        authSource = "AUTH",
        fullName = "Local Appointment Fixtures",
        lastSyncedAt = LocalDateTime.now(),
      ),
    )

  private fun generateCrn(): String = generateSequence {
    "X" + (1..6).joinToString(separator = "") { random.nextInt(10).toString() }
  }.first { personRepository.findByIdentifier(it) == null }

  private fun generateCaseReference(): String = generateSequence {
    referralReferenceGenerator.generate("AP")
  }.first { !referralRepository.existsByReferenceNumber(it) }
}

data class LocalAppointmentFixture(
  val caseReference: String,
  val referralId: UUID,
  val crn: String,
)
