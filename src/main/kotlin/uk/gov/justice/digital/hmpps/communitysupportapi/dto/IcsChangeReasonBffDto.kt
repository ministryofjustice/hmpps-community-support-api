package uk.gov.justice.digital.hmpps.communitysupportapi.dto

data class IcsChangeReasonBffDto(
  val fullName: String,
) {
  companion object {
    fun from(appointment: AppointmentIcsResponse): IcsChangeReasonBffDto = IcsChangeReasonBffDto(
      fullName = ReferralNameDto.fullName(appointment.referralFirstName, appointment.referralLastName),
    )
  }
}
