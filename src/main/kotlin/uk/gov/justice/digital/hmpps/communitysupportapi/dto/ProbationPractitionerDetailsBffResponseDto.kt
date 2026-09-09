package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.CommunityManagerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ProbationPractitionerDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.model.Pdu

data class ProbationPractitionerDetailsBffResponseDto(
  val name: String,
  val jobRole: String?,
  val emailAddress: String?,
  val pdu: Pdu?,
  val probationOffice: String?,
  val teamPhoneNumber: String?,
  val phoneNumber: String? = null,
  val ppDetailsFoundAndCorrect: Boolean? = null,
) {
  companion object {
    fun from(communityManager: CommunityManagerDto?, pdu: Pdu?): ProbationPractitionerDetailsBffResponseDto {
      val cmDetails = communityManager?.communityManager
      val name = cmDetails?.name

      return ProbationPractitionerDetailsBffResponseDto(
        name = listOfNotNull(name?.forename, name?.middleName, name?.surname).joinToString(" "),
        jobRole = cmDetails?.jobRole,
        emailAddress = cmDetails?.emailAddress,
        pdu = pdu,
        probationOffice = cmDetails?.officeName,
        teamPhoneNumber = cmDetails?.teamPhoneNumber,
      )
    }

    fun from(entity: ProbationPractitionerDetails, pdu: Pdu?): ProbationPractitionerDetailsBffResponseDto = ProbationPractitionerDetailsBffResponseDto(
      name = entity.name,
      jobRole = entity.jobRole,
      emailAddress = entity.emailAddress,
      pdu = pdu,
      probationOffice = entity.probationOffice,
      teamPhoneNumber = entity.teamPhoneNumber,
      phoneNumber = entity.phoneNumber,
      ppDetailsFoundAndCorrect = entity.ppDetailsFoundAndCorrect,
    )

    fun empty(): ProbationPractitionerDetailsBffResponseDto = ProbationPractitionerDetailsBffResponseDto(
      name = "",
      jobRole = null,
      emailAddress = null,
      pdu = null,
      probationOffice = null,
      teamPhoneNumber = null,
      phoneNumber = null,
      ppDetailsFoundAndCorrect = null,
    )
  }
}
