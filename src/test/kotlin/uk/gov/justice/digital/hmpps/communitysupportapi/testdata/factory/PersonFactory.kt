package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalDetails
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Factory for creating Person test entities with sensible defaults.
 * Use the builder pattern to customize individual properties.
 *
 * Example usage:
 * ```
 * // Create with all defaults
 * val person = PersonFactory().create()
 *
 * // Create with custom values
 * val person = PersonFactory()
 *     .withFirstName("Jane")
 *     .withLastName("Doe")
 *     .withIdentifier("CRN999999")
 *     .create()
 *
 * // Create multiple persons
 * val persons = PersonFactory().createMany(3) { index ->
 *     PersonFactory()
 *         .withFirstName("Person$index")
 *         .withIdentifier("CRN0000$index")
 *         .create()
 * }
 * ```
 */
class PersonFactory : TestEntityFactory<Person>() {

  private var id: UUID = UUID.randomUUID()
  private var identifier: String = "CRN${(100000..999999).random()}"
  private var firstName: String = "John"
  private var lastName: String = "Doe"
  private var dateOfBirth: LocalDate = LocalDate.of(1990, 1, 15)
  private var gender: String = "Male"
  private var createdAt: OffsetDateTime = OffsetDateTime.now()
  private var updatedAt: OffsetDateTime = OffsetDateTime.now()
  private var additionalDetails: PersonAdditionalDetails? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withIdentifier(identifier: String) = apply { this.identifier = identifier }
  fun withFirstName(firstName: String) = apply { this.firstName = firstName }
  fun withLastName(lastName: String) = apply { this.lastName = lastName }
  fun withDateOfBirth(dateOfBirth: LocalDate) = apply { this.dateOfBirth = dateOfBirth }
  fun withGender(gender: String) = apply { this.gender = gender }
  fun withCreatedAt(createdAt: OffsetDateTime) = apply { this.createdAt = createdAt }
  fun withUpdatedAt(updatedAt: OffsetDateTime) = apply { this.updatedAt = updatedAt }
  fun withAdditionalDetails(additionalDetails: PersonAdditionalDetails?) = apply { this.additionalDetails = additionalDetails }

  override fun create(): Person {
    val person = Person(
      id = id,
      identifier = identifier,
      firstName = firstName,
      lastName = lastName,
      dateOfBirth = dateOfBirth,
      gender = gender,
      createdAt = createdAt,
      updatedAt = updatedAt,
    )
    additionalDetails?.let { person.additionalDetails = it }
    return person
  }

  companion object {
    fun aDefaultPerson(): Person = PersonFactory().create()

    fun aMalePerson(): Person = PersonFactory()
      .withGender("Male")
      .create()

    fun aFemalePerson(): Person = PersonFactory()
      .withGender("Female")
      .withFirstName("Jane")
      .create()
  }
}
