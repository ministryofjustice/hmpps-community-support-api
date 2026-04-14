package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDelivery
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcs
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

data class CreateAppointmentRequest(
  val date: LocalDate,
  val time: AppointmentTimeRequest,
  val sessionMethodRequest: SessionMethodRequest,
  val sessionCommunication: List<String> = emptyList(),
)

data class AppointmentTimeRequest(
  /** 1–12 */
  val hour: Int,
  /** 0–59, defaults to 0 */
  val minute: Int? = 0,
  val amPm: String,
)

data class SessionMethodRequest(
  /**
   * Matches the UI options.
   * "PHONE" | "VIDEO" | "PROBATION_OFFICE" | "OTHER_LOCATION"
   */
  val type: SessionMethodType,
  val additionalDetails: String? = null,
  // PDU – only relevant when type is PROBATION_OFFICE
  val pdu: String? = null,
  // Address fields – only relevant when type is IN_PERSON_OTHER_LOCATION
  val addressLine1: String? = null,
  val addressLine2: String? = null,
  val townOrCity: String? = null,
  val county: String? = null,
  val postcode: String? = null,
)

enum class SessionMethodType {
  PHONE,
  VIDEO,
  PROBATION_OFFICE,
  OTHER_LOCATION,
}

// ── Session-method sealed hierarchy ──────────────────────────────────────────

/**
 * Discriminated union that the UI consumes to render the session-method section.
 *
 * ```
 * val enriched = ics.sessionMethod
 * when (enriched) {
 *   is VirtualAppointment   -> enriched.type        // "PHONE" | "VIDEO"
 *                              enriched.extraDetails // optional freetext
 *   is InPersonAppointment  -> enriched.type        // "IN_PERSON_PROBATION_OFFICE" | "IN_PERSON_OTHER_LOCATION"
 *                              enriched.address      // formatted address string
 * }
 * ```
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "appointmentCategory")
@JsonSubTypes(
  JsonSubTypes.Type(value = VirtualAppointment::class, name = "VIRTUAL"),
  JsonSubTypes.Type(value = InPersonAppointment::class, name = "IN_PERSON"),
)
@Schema(
  discriminatorProperty = "appointmentCategory",
  oneOf = [VirtualAppointment::class, InPersonAppointment::class],
  discriminatorMapping = [
    DiscriminatorMapping(value = "VIRTUAL", schema = VirtualAppointment::class),
    DiscriminatorMapping(value = "IN_PERSON", schema = InPersonAppointment::class),
  ],
)
sealed class SessionMethod {
  abstract val type: String
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VirtualAppointment(
  /** "PHONE" or "VIDEO" */
  override val type: String,
  /** e.g. "Why was this not in person?" */
  val whyNotInPersonReason: String? = null,
) : SessionMethod()

@JsonInclude(JsonInclude.Include.NON_NULL)
data class InPersonAppointment(
  /** "IN_PERSON_PROBATION_OFFICE" or "IN_PERSON_OTHER_LOCATION" */
  override val type: String,
  // For IN_PERSON_PROBATION_OFFICE cases
  val probationOfficeName: String? = null,
  // Structured address fields – only populated for IN_PERSON_OTHER_LOCATION
  val addressLine1: String? = null,
  val addressLine2: String? = null,
  val townOrCity: String? = null,
  val county: String? = null,
  val postcode: String? = null,
) : SessionMethod()

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AppointmentIcsResponse(
  val appointmentIcsId: UUID,
  val appointmentId: UUID,
  val referralId: UUID,
  val appointmentType: AppointmentType,
  val appointmentDate: LocalDate,
  val appointmentTime: AppointmentTimeResponse,
  val appointmentStatus: AppointmentStatusHistoryType,
  val sessionMethod: SessionMethod,
  val sessionCommunications: List<String>,
  val referralFirstName: String,
  val referralLastName: String,
  val createdAt: OffsetDateTime,
) {

  companion object {
    fun from(ics: AppointmentIcs, status: AppointmentStatusHistoryType, referralName: String): AppointmentIcsResponse {
      val appointmentDateTime = ics.appointmentDateTime
      val hour24 = appointmentDateTime.hour
      val amPm = if (hour24 < 12) "am" else "pm"
      val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
      }
      return AppointmentIcsResponse(
        appointmentIcsId = ics.id,
        appointmentId = ics.appointment.id,
        referralId = ics.appointment.referral.id,
        appointmentType = ics.appointment.type,
        appointmentDate = appointmentDateTime.toLocalDate(),
        appointmentTime = AppointmentTimeResponse(
          hour = hour12,
          minute = appointmentDateTime.minute,
          amPm = amPm,
        ),
        appointmentStatus = status,
        sessionMethod = buildSessionMethod(ics.appointmentDelivery),
        sessionCommunications = ics.sessionCommunication,
        referralFirstName = referralName.substringBefore(" "),
        referralLastName = referralName.substringAfter(" ", missingDelimiterValue = ""),
        createdAt = ics.createdAt.atOffset(ZoneOffset.UTC),
      )
    }

    private fun buildSessionMethod(delivery: AppointmentDelivery?): SessionMethod {
      if (delivery == null) return VirtualAppointment(type = "UNKNOWN")
      return when (delivery.method) {
        AppointmentDeliveryMethod.PHONE_CALL ->
          VirtualAppointment(type = "PHONE", whyNotInPersonReason = delivery.methodDetails)

        AppointmentDeliveryMethod.VIDEO_CALL ->
          VirtualAppointment(type = "VIDEO", whyNotInPersonReason = delivery.methodDetails)

        AppointmentDeliveryMethod.IN_PERSON_PROBATION_OFFICE ->
          InPersonAppointment(
            type = "IN_PERSON_PROBATION_OFFICE",
            // Office name is carried in methodDetails for probation-office appointments
            probationOfficeName = delivery.methodDetails,
          )

        AppointmentDeliveryMethod.IN_PERSON_OTHER_LOCATION ->
          InPersonAppointment(
            type = "IN_PERSON_OTHER_LOCATION",
            addressLine1 = delivery.addressLine1,
            addressLine2 = delivery.addressLine2,
            townOrCity = delivery.townOrCity,
            county = delivery.county,
            postcode = delivery.postcode,
          )
      }
    }
  }
}

data class AppointmentTimeResponse(
  val hour: Int,
  val minute: Int,
  val amPm: String,
)

/**
 * Converts the split date/time request fields into a [LocalTime].
 * The [AppointmentTimeRequest.hour] is a 12-hour value (1–12).
 */
fun AppointmentTimeRequest.toLocalTime(): LocalTime {
  val minuteValue = minute ?: 0
  val hour24 = when {
    amPm.lowercase() == "am" && hour == 12 -> 0
    amPm.lowercase() == "pm" && hour != 12 -> hour + 12
    else -> hour
  }
  return LocalTime.of(hour24, minuteValue)
}

/**
 * Maps [SessionMethodType] to the internal [AppointmentDeliveryMethod] enum.
 */
fun SessionMethodType.toDeliveryMethod(): AppointmentDeliveryMethod = when (this) {
  SessionMethodType.PHONE -> AppointmentDeliveryMethod.PHONE_CALL
  SessionMethodType.VIDEO -> AppointmentDeliveryMethod.VIDEO_CALL
  SessionMethodType.PROBATION_OFFICE -> AppointmentDeliveryMethod.IN_PERSON_PROBATION_OFFICE
  SessionMethodType.OTHER_LOCATION -> AppointmentDeliveryMethod.IN_PERSON_OTHER_LOCATION
}

/**
 * Returns a human-readable label for the session method type, used when persisting
 * feedback for how a session took place.
 */
fun SessionMethodType.toSessionDisplayString(): String = when (this) {
  SessionMethodType.PHONE -> "Phone call"
  SessionMethodType.VIDEO -> "Video call"
  SessionMethodType.PROBATION_OFFICE -> "In person (probation office)"
  SessionMethodType.OTHER_LOCATION -> "In person (other location)"
}
