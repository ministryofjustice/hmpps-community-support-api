package uk.gov.justice.digital.hmpps.communitysupportapi.validation

import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier

class PersonIdentifierValidatorTest {

  private val validator = PersonIdentifierValidator()

  @Test
  fun `throws ValidationException when identifier is null`() {
    val exception = assertThrows<ValidationException> {
      validator.validate(null)
    }

    assertThat(exception.message)
      .isEqualTo("Person Identifier must be provided")
  }

  @Test
  fun `throws ValidationException when identifier is empty`() {
    val exception = assertThrows<ValidationException> {
      validator.validate("")
    }

    assertThat(exception.message)
      .isEqualTo("Person Identifier must be provided")
  }

  @Test
  fun `throws ValidationException when identifier format is invalid`() {
    val exception = assertThrows<ValidationException> {
      validator.validate("1234567")
    }

    assertThat(exception.message)
      .isEqualTo("Invalid Person Identifier format")
  }

  @Test
  fun `returns Crn when identifier matches CRN pattern`() {
    val result = validator.validate("X123456")

    assertThat(result)
      .isInstanceOf(PersonIdentifier.Crn::class.java)

    assertThat((result as PersonIdentifier.Crn).value)
      .isEqualTo("X123456")
  }

  @Test
  fun `returns Prisoner Number when identifier matches Prisoner Number pattern`() {
    val result = validator.validate("A1234BC")

    assertThat(result)
      .isInstanceOf(PersonIdentifier.PrisonerNumber::class.java)

    assertThat((result as PersonIdentifier.PrisonerNumber).value)
      .isEqualTo("A1234BC")
  }
}
