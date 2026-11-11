package uk.gov.justice.digital.hmpps.communitysupportapi.validation.appointments

import jakarta.validation.ValidationException
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType

@Component
class CreateAppointmentValidator {
  companion object {

    private val allLocationMethods = setOf(
      SessionMethodType.PHONE,
      SessionMethodType.VIDEO,
      SessionMethodType.IN_PERSON_PROBATION_OFFICE,
      SessionMethodType.IN_PERSON_OTHER_LOCATION,
    )

    /** Session methods permitted for each appointment type (see session-mapping.png). */
    private val allowedSessionMethods: Map<AppointmentType, Set<SessionMethodType>> = mapOf(
      AppointmentType.ICS to allLocationMethods,
      AppointmentType.CONTACT_SESSION to allLocationMethods,
      AppointmentType.POST_RELEASE_SESSION to allLocationMethods,
      AppointmentType.PRE_RELEASE_SESSION to setOf(
        SessionMethodType.PHONE,
        SessionMethodType.VIDEO,
        SessionMethodType.IN_PERSON_OTHER_LOCATION,
        SessionMethodType.IN_PERSON_PRISON,
      ),
      AppointmentType.HANDOVER_SESSION to setOf(
        SessionMethodType.PHONE,
        SessionMethodType.VIDEO,
        SessionMethodType.IN_PERSON,
      ),
    )

    /**
     * Validates the request against the form rules for the given appointment type (see session-mapping.png).
     */
    fun validate(type: AppointmentType, request: CreateAppointmentRequest) {
      val sessionMethod = request.sessionMethodRequest

      val allowedMethods = allowedSessionMethods.getValue(type)
      if (sessionMethod.type !in allowedMethods) {
        throw ValidationException("Session method ${sessionMethod.type} is not allowed for appointment type $type")
      }

      if (sessionMethod.type == SessionMethodType.IN_PERSON_OTHER_LOCATION) {
        val missingFields = listOfNotNull(
          "addressLine1".takeIf { sessionMethod.addressLine1.isNullOrBlank() },
          "townOrCity".takeIf { sessionMethod.townOrCity.isNullOrBlank() },
          "postcode".takeIf { sessionMethod.postcode.isNullOrBlank() },
        )
        if (missingFields.isNotEmpty()) {
          throw ValidationException("Missing required address fields for IN_PERSON_OTHER_LOCATION: ${missingFields.joinToString()}")
        }
      }

      if (sessionMethod.type == SessionMethodType.IN_PERSON_PRISON && sessionMethod.prisonCode.isNullOrBlank()) {
        throw ValidationException("prisonCode is required for IN_PERSON_PRISON")
      }

      if (type == AppointmentType.POST_RELEASE_SESSION) {
        if (request.meetAtTheGate == null) {
          throw ValidationException("meetAtTheGate is required for appointment type $type")
        }
      } else if (request.meetAtTheGate != null) {
        throw ValidationException("meetAtTheGate is only applicable to appointment type ${AppointmentType.POST_RELEASE_SESSION}")
      }

      if (type == AppointmentType.HANDOVER_SESSION && request.sessionCommunication.isNotEmpty()) {
        throw ValidationException("sessionCommunication is not applicable to appointment type $type")
      }
    }
  }
}
