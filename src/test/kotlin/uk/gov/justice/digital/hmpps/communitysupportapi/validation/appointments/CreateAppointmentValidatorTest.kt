package uk.gov.justice.digital.hmpps.communitysupportapi.validation.appointments

import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentTimeRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import java.time.LocalDate

class CreateAppointmentValidatorTest {
  private val otherLocation = SessionMethodRequest(
    type = SessionMethodType.IN_PERSON_OTHER_LOCATION,
    addressLine1 = "1 High Street",
    addressLine2 = "Flat 2",
    townOrCity = "Leeds",
    county = "West Yorkshire",
    postcode = "LS1 1AA",
  )

  private fun buildTypedRequest(
    type: AppointmentType,
    sessionMethodRequest: SessionMethodRequest = SessionMethodRequest(type = SessionMethodType.PHONE),
    sessionCommunication: List<String> = if (type == AppointmentType.HANDOVER_SESSION) emptyList() else listOf("Email"),
    meetAtTheGate: Boolean? = if (type == AppointmentType.POST_RELEASE_SESSION) false else null,
  ) = CreateAppointmentRequest(
    appointmentType = type,
    date = LocalDate.of(2026, 3, 27),
    time = AppointmentTimeRequest(hour = 10, minute = 0, amPm = "am"),
    sessionMethodRequest = sessionMethodRequest,
    sessionCommunication = sessionCommunication,
    meetAtTheGate = meetAtTheGate,
  )

  @ParameterizedTest
  @EnumSource(value = AppointmentType::class, names = ["PRE_RELEASE_SESSION", "HANDOVER_SESSION"])
  fun `should reject in-person probation office for pre-release and handover sessions`(type: AppointmentType) {
    val request = buildTypedRequest(type, SessionMethodRequest(type = SessionMethodType.IN_PERSON_PROBATION_OFFICE))

    assertThatThrownBy { CreateAppointmentValidator.validate(type, request) }
      .isInstanceOf(ValidationException::class.java)
      .hasMessage("Session method IN_PERSON_PROBATION_OFFICE is not allowed for appointment type $type")
  }

  @Test
  fun `should reject in-person other location for handover session`() {
    val type = AppointmentType.HANDOVER_SESSION

    assertThatThrownBy { CreateAppointmentValidator.validate(type, buildTypedRequest(type, otherLocation)) }
      .isInstanceOf(ValidationException::class.java)
      .hasMessage("Session method IN_PERSON_OTHER_LOCATION is not allowed for appointment type HANDOVER_SESSION")
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType::class, names = ["HANDOVER_SESSION"], mode = EnumSource.Mode.EXCLUDE)
  fun `should reject in-person without location for non-handover types`(type: AppointmentType) {
    val request = buildTypedRequest(type, SessionMethodRequest(type = SessionMethodType.IN_PERSON))

    assertThatThrownBy { CreateAppointmentValidator.validate(type, request) }
      .isInstanceOf(ValidationException::class.java)
      .hasMessage("Session method IN_PERSON is not allowed for appointment type $type")
  }

  @Test
  fun `should reject session communication for handover session`() {
    val type = AppointmentType.HANDOVER_SESSION
    val request = buildTypedRequest(type, sessionCommunication = listOf("Email"))

    assertThatThrownBy { CreateAppointmentValidator.validate(type, request) }
      .isInstanceOf(ValidationException::class.java)
      .hasMessage("sessionCommunication is not applicable to appointment type HANDOVER_SESSION")
  }

  @Test
  fun `should require meet at the gate for post-release session`() {
    val type = AppointmentType.POST_RELEASE_SESSION

    assertThatThrownBy { CreateAppointmentValidator.validate(type, buildTypedRequest(type, meetAtTheGate = null)) }
      .isInstanceOf(ValidationException::class.java)
      .hasMessage("meetAtTheGate is required for appointment type POST_RELEASE_SESSION")
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType::class, names = ["POST_RELEASE_SESSION"], mode = EnumSource.Mode.EXCLUDE)
  fun `should reject meet at the gate for non post-release types`(type: AppointmentType) {
    assertThatThrownBy { CreateAppointmentValidator.validate(type, buildTypedRequest(type, meetAtTheGate = true)) }
      .isInstanceOf(ValidationException::class.java)
      .hasMessage("meetAtTheGate is only applicable to appointment type POST_RELEASE_SESSION")
  }

  @Test
  fun `should reject in-person other location with missing mandatory address fields`() {
    val type = AppointmentType.CONTACT_SESSION
    val request = buildTypedRequest(type, SessionMethodRequest(type = SessionMethodType.IN_PERSON_OTHER_LOCATION, addressLine1 = "1 High Street"))

    assertThatThrownBy { CreateAppointmentValidator.validate(type, request) }
      .isInstanceOf(ValidationException::class.java)
      .hasMessage("Missing required address fields for IN_PERSON_OTHER_LOCATION: townOrCity, postcode")
  }
}
