package uk.gov.justice.digital.hmpps.communitysupportapi.model

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcsFeedback

enum class AppointmentStatusHistoryType {
  SCHEDULED,
  NEEDS_FEEDBACK,
  COMPLETED,
  CHANGED,
  DID_NOT_ATTEND,
  DID_NOT_HAPPEN,
}

/**
 * Derives the [AppointmentStatusHistoryType] from the submitted feedback:
 *
 * - `didSessionHappen == true`                                                     → COMPLETED
 * - `didSessionHappen == false` AND `didPersonAttend == false`                     → DID_NOT_ATTEND
 * - `didSessionHappen == false` AND `didPersonAttend == true`
 *   AND `sessionNotHappenReason != null`                                           → DID_NOT_HAPPEN
 */
fun AppointmentIcsFeedback.getAppointmentStatusHistoryType(): AppointmentStatusHistoryType = when {
  recordSessionDidSessionHappen -> AppointmentStatusHistoryType.COMPLETED
  recordSessionDidPersonAttend == false -> AppointmentStatusHistoryType.DID_NOT_ATTEND
  else -> AppointmentStatusHistoryType.DID_NOT_HAPPEN
}
