package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.CommunityManagerDetailsDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.CommunityManagerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.CommunityManagerNameDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ProbationPractitionerDetails
import java.time.OffsetDateTime
import java.util.UUID

class ProbationPractitionerDetailsBffResponseDtoTest {

  @Test
  fun `from CommunityManagerDto should join forename, middle name and surname into name`() {
    val response = CommunityManagerDto(
      crn = "X123456",
      communityManager = CommunityManagerDetailsDto(
        jobRole = "Probation practitioner",
        emailAddress = "jane.doe@example.com",
        pdu = "Northumberland",
        officeName = "Newcastle Office",
        name = CommunityManagerNameDto(forename = "Jane", middleName = "Middle", surname = "Doe"),
        teamPhoneNumber = "0123456789",
      ),
    )

    val result = ProbationPractitionerDetailsBffResponseDto.from(response)

    result.name shouldBe "Jane Middle Doe"
    result.jobRole shouldBe "Probation practitioner"
    result.emailAddress shouldBe "jane.doe@example.com"
    result.pdu shouldBe "Northumberland"
    result.probationOffice shouldBe "Newcastle Office"
    result.teamPhoneNumber shouldBe "0123456789"
  }

  @Test
  fun `from CommunityManagerDto should return empty name when community manager is missing`() {
    val response = CommunityManagerDto(crn = "X123456", communityManager = null)

    val result = ProbationPractitionerDetailsBffResponseDto.from(response)

    result.name shouldBe ""
    result.jobRole shouldBe null
    result.emailAddress shouldBe null
    result.pdu shouldBe null
    result.probationOffice shouldBe null
    result.teamPhoneNumber shouldBe null
  }

  @Test
  fun `from entity should map persisted probation practitioner details`() {
    val entity = ProbationPractitionerDetails(
      id = UUID.randomUUID(),
      referralId = UUID.randomUUID(),
      name = "Jane Doe",
      jobRole = "Probation practitioner",
      emailAddress = "jane.doe@example.com",
      pdu = "Northumberland",
      probationOffice = "Newcastle Office",
      teamPhoneNumber = "0123456789",
      updatedAt = OffsetDateTime.now(),
      updatedBy = UUID.randomUUID(),
    )

    val result = ProbationPractitionerDetailsBffResponseDto.from(entity)

    result.name shouldBe "Jane Doe"
    result.jobRole shouldBe "Probation practitioner"
    result.emailAddress shouldBe "jane.doe@example.com"
    result.pdu shouldBe "Northumberland"
    result.probationOffice shouldBe "Newcastle Office"
    result.teamPhoneNumber shouldBe "0123456789"
  }
}
