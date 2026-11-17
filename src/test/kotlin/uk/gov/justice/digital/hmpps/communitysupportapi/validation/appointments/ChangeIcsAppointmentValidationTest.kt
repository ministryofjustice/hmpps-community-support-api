package uk.gov.justice.digital.hmpps.communitysupportapi.validation.appointments

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentTimeRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodType
import java.time.LocalDate

class ChangeIcsAppointmentValidationTest {
  @Test
  fun `only prison is rejected when changing an ICS appointment`() {
    Validation.buildDefaultValidatorFactory().use { factory ->
      val validator = factory.validator
      SessionMethodType.entries.forEach { method ->
        val violations = validator.validate(request(method), ChangeIcsAppointment::class.java)
        if (method == SessionMethodType.IN_PERSON_PRISON) {
          assertThat(violations).hasSize(1)
          assertThat(violations.single().message)
            .isEqualTo("Session method IN_PERSON_PRISON is not allowed for appointment type ICS")
        } else {
          assertThat(violations).describedAs("method %s", method).isEmpty()
        }
      }
    }
  }

  @Test
  fun `prison is not rejected by the default validation group`() {
    Validation.buildDefaultValidatorFactory().use { factory ->
      assertThat(factory.validator.validate(request(SessionMethodType.IN_PERSON_PRISON))).isEmpty()
    }
  }

  @Test
  fun `validation property is not included in request JSON`() {
    val json = jacksonObjectMapper().findAndRegisterModules().writeValueAsString(request(SessionMethodType.PHONE))

    assertThat(json).doesNotContain("validIcsChangeSessionMethod")
  }

  private fun request(method: SessionMethodType) = CreateAppointmentRequest(
    date = LocalDate.of(2026, 3, 27),
    time = AppointmentTimeRequest(hour = 10, amPm = "am"),
    sessionMethodRequest = SessionMethodRequest(type = method),
  )
}
