package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import java.time.OffsetDateTime
import java.util.UUID

class PersonAdditionalSupportNeedsFactory : TestEntityFactory<PersonAdditionalSupportNeeds>() {
  private var id: UUID = UUID.randomUUID()
  private lateinit var referral: Referral
  private lateinit var person: Person

  private var noAdditionalSupportNeeded: Boolean = false

  private var physicalHealthDetails: String? = null
  private var mentalEmotionalHealthDetails: String? = null
  private var neurodiversityDetails: String? = null
  private var locationTravelDetails: String? = null
  private var caringResponsibilitiesDetails: String? = null
  private var employmentResponsibilitiesDetails: String? = null
  private var diversityDetails: String? = null
  private var anythingElseDetails: String? = null

  private var interpreterLanguage: String? = null

  private var createdAt: OffsetDateTime = OffsetDateTime.now()
  private var updatedAt: OffsetDateTime = OffsetDateTime.now()
  private lateinit var createdBy: UUID
  private var updatedBy: UUID? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferral(referral: Referral) = apply { this.referral = referral }
  fun withPerson(person: Person) = apply { this.person = person }

  fun noAdditionalSupportNeeded(noAdditionalSupportNeeded: Boolean) = apply {
    this.noAdditionalSupportNeeded = noAdditionalSupportNeeded
    if (noAdditionalSupportNeeded) {
      this.physicalHealthDetails = null
      this.mentalEmotionalHealthDetails = null
      this.neurodiversityDetails = null
      this.locationTravelDetails = null
      this.caringResponsibilitiesDetails = null
      this.employmentResponsibilitiesDetails = null
      this.diversityDetails = null
      this.anythingElseDetails = null
    }
  }

  fun withPhysicalHealthDetails(details: String?) = apply { this.physicalHealthDetails = details }
  fun withMentalEmotionalHealthDetails(details: String?) = apply { this.mentalEmotionalHealthDetails = details }
  fun withNeurodiversityDetails(details: String?) = apply { this.neurodiversityDetails = details }
  fun withLocationTravelDetails(details: String?) = apply { this.locationTravelDetails = details }
  fun withCaringResponsibilitiesDetails(details: String?) = apply { this.caringResponsibilitiesDetails = details }
  fun withEmploymentResponsibilitiesDetails(details: String?) = apply { this.employmentResponsibilitiesDetails = details }
  fun withDiversityDetails(details: String?) = apply { this.diversityDetails = details }
  fun withAnythingElseDetails(details: String?) = apply { this.anythingElseDetails = details }
  fun withNoAdditionalSupportNeeded(noAdditionalSupportNeeded: Boolean) = apply { this.noAdditionalSupportNeeded = noAdditionalSupportNeeded }
  fun withInterpreterLanguage(language: String?) = apply { this.interpreterLanguage = language }

  fun withCreatedBy(userId: UUID) = apply { this.createdBy = userId }
  fun withUpdatedBy(userId: UUID?) = apply { this.updatedBy = userId }

  override fun create(): PersonAdditionalSupportNeeds = PersonAdditionalSupportNeeds(
    id = id,
    referralId = referral.id,
    personId = person.id,
    noAdditionalSupportNeeded = noAdditionalSupportNeeded,
    physicalHealthDetails = physicalHealthDetails,
    mentalEmotionalHealthDetails = mentalEmotionalHealthDetails,
    neurodiversityDetails = neurodiversityDetails,
    locationTravelDetails = locationTravelDetails,
    caringResponsibilitiesDetails = caringResponsibilitiesDetails,
    employmentResponsibilitiesDetails = employmentResponsibilitiesDetails,
    diversityDetails = diversityDetails,
    anythingElseDetails = anythingElseDetails,
    interpreterLanguage = interpreterLanguage,
    createdAt = createdAt,
    updatedAt = updatedAt,
    createdBy = createdBy,
    updatedBy = updatedBy,
  )
}
