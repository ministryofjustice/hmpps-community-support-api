package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalDetails
import java.util.UUID

class PersonAdditionalDetailsFactory : TestEntityFactory<PersonAdditionalDetails>() {

  private var id: UUID = UUID.randomUUID()
  private lateinit var person: Person
  private var ethnicity: String? = null
  private var preferredLanguage: String? = null
  private var neurodiverseConditions: String? = null
  private var religionOrBelief: String? = null
  private var address: String? = null
  private var phoneNumber: String? = null
  private var emailAddress: String? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withPerson(person: Person) = apply { this.person = person }
  fun withEthnicity(ethnicity: String?) = apply { this.ethnicity = ethnicity }
  fun withPreferredLanguage(preferredLanguage: String?) = apply { this.preferredLanguage = preferredLanguage }
  fun withNeurodiverseConditions(neurodiverseConditions: String?) = apply { this.neurodiverseConditions = neurodiverseConditions }
  fun withReligionOrBelief(religionOrBelief: String?) = apply { this.religionOrBelief = religionOrBelief }
  fun withAddress(address: String?) = apply { this.address = address }
  fun withPhoneNumber(phoneNumber: String?) = apply { this.phoneNumber = phoneNumber }
  fun withEmailAddress(emailAddress: String?) = apply { this.emailAddress = emailAddress }

  override fun create(): PersonAdditionalDetails {
    check(::person.isInitialized) { "Person must be set before creating PersonAdditionalDetails" }

    return PersonAdditionalDetails(
      id = id,
      person = person,
      ethnicity = ethnicity,
      preferredLanguage = preferredLanguage,
      neurodiverseConditions = neurodiverseConditions,
      religionOrBelief = religionOrBelief,
      address = address,
      phoneNumber = phoneNumber,
      emailAddress = emailAddress,
    )
  }

  companion object {
    fun withCommonDetails(person: Person): PersonAdditionalDetails = PersonAdditionalDetailsFactory()
      .withPerson(person)
      .withEthnicity("White British")
      .withPreferredLanguage("English")
      .create()
  }
}
