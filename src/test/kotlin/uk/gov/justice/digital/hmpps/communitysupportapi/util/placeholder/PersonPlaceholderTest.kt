package uk.gov.justice.digital.hmpps.communitysupportapi.util.placeholder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.util.PlaceholderUtils
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class PersonPlaceholderTest {

  @Test
  fun `resolve should return first, last and full name values`() {
    val now = OffsetDateTime.now()
    val person = Person(
      id = UUID.randomUUID(),
      identifier = "X123456",
      firstName = "Alice",
      lastName = "Turner",
      dateOfBirth = LocalDate.of(2000, 1, 2),
      gender = "Female",
      createdAt = now,
      updatedAt = now,
    )

    val placeholder = PersonPlaceholder(person)

    assertEquals(
      mapOf(
        "firstName" to "Alice",
        "lastName" to "Turner",
        "fullName" to "Alice Turner",
      ),
      placeholder.resolve(setOf("firstName", "lastName", "fullName")),
    )
  }

  @Test
  fun `render should replace full name placeholder`() {
    val now = OffsetDateTime.now()
    val person = Person(
      id = UUID.randomUUID(),
      identifier = "X123456",
      firstName = "Alice",
      lastName = "Turner",
      dateOfBirth = LocalDate.of(2000, 1, 2),
      gender = "Female",
      createdAt = now,
      updatedAt = now,
    )

    val result = PlaceholderUtils.render("Welcome {{ fullName }}!", PersonPlaceholder(person))

    assertEquals("Welcome Alice Turner!", result)
  }

  @Test
  fun `resolve should ignore unsupported tokens`() {
    val now = OffsetDateTime.now()
    val person = Person(
      id = UUID.randomUUID(),
      identifier = "X123456",
      firstName = "Alice",
      lastName = "Turner",
      dateOfBirth = LocalDate.of(2000, 1, 2),
      gender = "Female",
      createdAt = now,
      updatedAt = now,
    )

    val result = PersonPlaceholder(person).resolve(setOf("service_end_date"))

    assertEquals(emptyMap<String, String>(), result)
  }

  @Test
  fun `neededFor should detect supported placeholder names`() {
    assertTrue(PersonPlaceholder.neededFor(setOf("firstName")))
    assertTrue(PersonPlaceholder.neededFor(setOf("lastName")))
    assertTrue(PersonPlaceholder.neededFor(setOf("fullName")))
    assertFalse(PersonPlaceholder.neededFor(setOf("service_end_date")))
  }
}
