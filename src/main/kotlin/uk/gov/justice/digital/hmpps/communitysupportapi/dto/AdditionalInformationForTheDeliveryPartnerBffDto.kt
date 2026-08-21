package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person

data class AdditionalInformationForTheDeliveryPartnerBffDto(
  val refereeName: RefereeNameDto,
  val details: Selection,
) {
  companion object {
    fun from(person: Person): AdditionalInformationForTheDeliveryPartnerBffDto {
      val refereeName = RefereeNameDto(firstName = person.firstName, lastName = person.lastName)
      //stub
      return AdditionalInformationForTheDeliveryPartnerBffDto( refereeName, Selection.Unanswered );
    }
  }
}
