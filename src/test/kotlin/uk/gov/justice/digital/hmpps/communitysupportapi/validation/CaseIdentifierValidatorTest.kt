package uk.gov.justice.digital.hmpps.communitysupportapi.validation

import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CaseIdentifier
import java.util.UUID

class CaseIdentifierValidatorTest {

  private val validator = CaseIdentifierValidator()

  @Test
  fun `throws ValidationException when identifier is null`() {
    val exception = assertThrows<ValidationException> {
      validator.validate(null)
    }

    assertThat(exception.message)
      .isEqualTo("Case identifier must be provided")
  }

  @Test
  fun `throws ValidationException when identifier is empty`() {
    val exception = assertThrows<ValidationException> {
      validator.validate("")
    }

    assertThat(exception.message)
      .isEqualTo("Case identifier must be provided")
  }

  @Test
  fun `throws ValidationException when identifier format is invalid`() {
    val exception = assertThrows<ValidationException> {
      validator.validate("1234567")
    }

    assertThat(exception.message)
      .isEqualTo("Invalid Case Identifier format")
  }

  @Test
  fun `returns Case Id when identifier matches Case Id pattern`() {
    val result = validator.validate("AA1234BB")

    assertThat(result)
      .isInstanceOf(CaseIdentifier.CaseId::class.java)

    assertThat((result as CaseIdentifier.CaseId).value)
      .isEqualTo("AA1234BB")
  }

  @Test
  fun `returns Prisoner Number when identifier matches Prisoner Number pattern`() {
    val referralId = UUID.randomUUID()
    val result = validator.validate(referralId.toString())

    assertThat(result)
      .isInstanceOf(CaseIdentifier.ReferralId::class.java)

    assertThat((result as CaseIdentifier.ReferralId).value)
      .isEqualTo(referralId)
  }
}
