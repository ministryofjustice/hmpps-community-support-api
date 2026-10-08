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
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralProviderAssignment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

@Service
@Profile("local")
class LocalAppointmentFixtureService(
  private val personRepository: PersonRepository,
  private val referralRepository: ReferralRepository,
  private val referralUserRepository: ReferralUserRepository,
  private val localWireMockFixtureClient: LocalWireMockFixtureClient,
  private val referralReferenceGenerator: ReferralReferenceGenerator,
  private val serviceProviderRepository: CommunityServiceProviderRepository,
  private val referralProviderAssignmentRepository: ReferralProviderAssignmentRepository,
) {
  companion object {
    private const val FIXTURE_USERNAME = "local-appointment-fixtures"
    private val random = SecureRandom()
  }

  @Transactional
  fun createFixtures(count: Int, user: ReferralUser): List<LocalAppointmentFixture> {
    val fixtureUser = findOrCreateFixtureUser(user)

    /**
     * `SEETEC_BUS_TECH_CTR_LTD` is the name of the service provider in the database,
     * (UUID=`a1b2c3d4-e5f6-4a7b-8c9d-1a2b3c4d5e6f`)
     *
     * `bc852b9d-1997-4ce4-ba7f-cd1759e15d2b` is the UUID of `Community Support Service in Cleveland`
     * Users assigned to this service provider will be able to see the appointments created by this fixture service.
     *
     */
    val serviceProvider = serviceProviderRepository.getReferenceById(UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")) ?: throw IllegalStateException("Service provider not found")

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

      referralProviderAssignmentRepository.saveAndFlush(
        ReferralProviderAssignment(
          UUID.randomUUID(),
          referral,
          serviceProvider,
        ),
      )

      LocalAppointmentFixture(
        caseReference = caseReference,
        referralId = referral.id,
        crn = crn,
      )
    }
  }

  private fun findOrCreateFixtureUser(
    user: ReferralUser,
  ): ReferralUser = referralUserRepository.findByHmppsAuthUsernameIgnoreCase(FIXTURE_USERNAME)
    ?: referralUserRepository.save(
      ReferralUser(
        hmppsAuthId = user.hmppsAuthId,
        hmppsAuthUsername = user.hmppsAuthUsername,
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
