package uk.gov.justice.digital.hmpps.communitysupportapi.validation.appointments

import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateIcsFeedbackRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.RecordSessionRequest

class CreateIcsFeedbackValidatorTest {
  @Test
  fun `rejects missing or blank answers when person did not attend`() {
    val invalidValues = listOf(null, "", "  ")
    invalidValues.forEach { value ->
      assertThatThrownBy {
        CreateIcsFeedbackValidator.validate(request(false, false, value, "Unwell"))
      }.isInstanceOf(ValidationException::class.java)
        .hasMessageContaining("howTheyTriedToContactThePerson is required")

      assertThatThrownBy {
        CreateIcsFeedbackValidator.validate(request(false, false, "Called", value))
      }.isInstanceOf(ValidationException::class.java)
        .hasMessageContaining("reasonsTheyDidNotAttend is required")
    }

    assertThatThrownBy {
      CreateIcsFeedbackValidator.validate(request(false, false, null, null))
    }.isInstanceOf(ValidationException::class.java)
      .hasMessageContaining("howTheyTriedToContactThePerson is required")
      .hasMessageContaining("reasonsTheyDidNotAttend is required")
  }

  @Test
  fun `accepts both answers and other attendance paths`() {
    val validRequests = listOf(
      request(false, false, "Called", "Unwell"),
      request(false, true, null, null),
      request(false, null, null, null),
      request(true, false, null, null),
      request(true, null, null, null),
    )

    assertThat(validRequests).allSatisfy { CreateIcsFeedbackValidator.validate(it) }
  }

  private fun request(
    didSessionHappen: Boolean,
    didPersonAttend: Boolean?,
    contact: String?,
    reasons: String?,
  ) = CreateIcsFeedbackRequest(
    record = RecordSessionRequest(
      didSessionHappen = didSessionHappen,
      didPersonAttend = didPersonAttend,
      howTheyTriedToContactThePerson = contact,
      reasonsTheyDidNotAttend = reasons,
    ),
  )
}
