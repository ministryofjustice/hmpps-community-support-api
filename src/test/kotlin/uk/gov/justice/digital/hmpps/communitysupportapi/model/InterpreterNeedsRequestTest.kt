package uk.gov.justice.digital.hmpps.communitysupportapi.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InterpreterNeedsRequestTest {

  @Test
  fun `normalised should keep details when interpreter is needed`() {
    val request = NeedsInterpreterRequest(
      language = "Spanish",
      needsInterpreter = true,
    )

    val normalised = request.normaliseAgainstNeedsInterpreter()

    assertThat(normalised).isSameAs(request)
  }

  @Test
  fun `normalised should clear details when interpreter is not needed`() {
    val request = NeedsInterpreterRequest(
      language = "French",
      needsInterpreter = false,
    )

    val normalised = request.normaliseAgainstNeedsInterpreter()

    assertThat(normalised.needsInterpreter).isFalse()
    assertThat(normalised.language).isNull()
  }

  @Test
  fun `normalised should clear details when needs interpreter is null`() {
    val request = NeedsInterpreterRequest(
      language = "Polish",
      needsInterpreter = null,
    )

    val normalised = request.normaliseAgainstNeedsInterpreter()

    assertThat(normalised.needsInterpreter).isNull()
    assertThat(normalised.language).isNull()
  }
}
