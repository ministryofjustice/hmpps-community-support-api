package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import java.time.LocalDate
import java.util.UUID

data class ReferralDto(
  val id: UUID,
  val crn: String?,
  val referenceNumber: String?,
  val createdDate: java.time.OffsetDateTime,
) {
  companion object {
    fun from(referral: Referral): ReferralDto = ReferralDto(
      id = referral.id,
      crn = referral.crn,
      referenceNumber = referral.referenceNumber,
      createdDate = referral.createdAt,
    )
  }
}

data class ReferralNameDto(
  val firstName: String,
  val lastName: String,
) {
  companion object {
    fun fullName(firstName: String, lastName: String): String = "$firstName $lastName"
  }
}

data class ReferralInformationDto(
  val personId: UUID,
  val referralId: UUID,
  val referralDate: LocalDate,
  val firstName: String?,
  val lastName: String?,
  val sex: String? = null,
  val crn: String,
  val communityServiceProviderId: UUID,
  val communityServiceProviderName: String,
  val region: String,
  val referenceNumber: String? = null,
  val deliveryPartner: String,
) {
  companion object {
    fun from(result: ReferralCreationResult): ReferralInformationDto = ReferralInformationDto(
      personId = result.referral.personId,
      referralId = result.referral.id,
      referralDate = result.referral.createdAt.toLocalDate(),
      communityServiceProviderId = result.communityServiceProvider.id,
      firstName = result.person.firstName,
      lastName = result.person.lastName,
      sex = result.person.gender,
      crn = result.referral.crn,
      communityServiceProviderName = result.communityServiceProvider.name,
      region = result.communityServiceProvider.contractArea.region.name,
      deliveryPartner = result.communityServiceProvider.serviceProvider.name,
    )
  }
}

data class ReferralCreationResult(
  val referral: Referral,
  val person: Person,
  val communityServiceProvider: CommunityServiceProvider,
)

data class SubmitReferralResponseDto(
  val referralId: UUID,
  val referenceNumber: String?,
)

fun Referral.toDto() = ReferralDto.from(this)
fun ReferralCreationResult.toReferralInformationDto(): ReferralInformationDto = ReferralInformationDto.from(this)
