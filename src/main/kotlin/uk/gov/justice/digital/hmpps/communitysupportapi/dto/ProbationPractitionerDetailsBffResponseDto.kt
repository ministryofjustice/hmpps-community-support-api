package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.CommunityManagerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ProbationPractitionerDetails

data class ProbationPractitionerDetailsBffResponseDto(
  val name: String,
  val jobRole: String?,
  val emailAddress: String?,
  val pdu: String?,
  val probationOffice: String?,
  val teamPhoneNumber: String?,
  val phoneNumber: String? = null,
  val ppDetailsFoundAndCorrect: Boolean? = null,
) {
  companion object {
    fun from(response: CommunityManagerDto): ProbationPractitionerDetailsBffResponseDto {
      val communityManager = response.communityManager
      val name = communityManager?.name

      return ProbationPractitionerDetailsBffResponseDto(
        name = listOfNotNull(name?.forename, name?.middleName, name?.surname).joinToString(" "),
        jobRole = communityManager?.jobRole,
        emailAddress = communityManager?.emailAddress,
        pdu = communityManager?.pdu,
        probationOffice = communityManager?.officeName,
        teamPhoneNumber = communityManager?.teamPhoneNumber,
      )
    }

    fun from(entity: ProbationPractitionerDetails): ProbationPractitionerDetailsBffResponseDto = ProbationPractitionerDetailsBffResponseDto(
      name = entity.name,
      jobRole = entity.jobRole,
      emailAddress = entity.emailAddress,
      pdu = entity.pdu,
      probationOffice = entity.probationOffice,
      teamPhoneNumber = entity.teamPhoneNumber,
      phoneNumber = entity.phoneNumber,
      ppDetailsFoundAndCorrect = entity.ppDetailsFoundAndCorrect,
    )
  }
}
