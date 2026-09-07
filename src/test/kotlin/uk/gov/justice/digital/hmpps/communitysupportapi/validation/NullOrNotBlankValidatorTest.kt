package uk.gov.justice.digital.hmpps.communitysupportapi.validation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class NullOrNotBlankValidatorTest {

  private val validator = NullOrNotBlankValidator()

  @Test
  fun `null is valid`() {
    assertThat(validator.isValid(null, mock())).isTrue()
  }

  @Test
  fun `non-blank is valid`() {
    assertThat(validator.isValid("some data", mock())).isTrue()
  }

  @Test
  fun `empty is invalid`() {
    assertThat(validator.isValid("", mock())).isFalse()
  }

  @Test
  fun `whitespace is invalid`() {
    assertThat(validator.isValid("   ", mock())).isFalse()
  }
}
