package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralWithdrawalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import java.time.OffsetDateTime
import java.util.UUID

class ReferralWithdrawalDetailsRepositoryTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralWithdrawalDetailsRepository: ReferralWithdrawalDetailsRepository

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Test
  fun `should save withdrawal details when reason code matches a known withdrawal reason name`() {
    val referralUser = referralHelper.ensureReferralUser()
    val referral = referralHelper.createReferral(submittedBy = referralUser)

    val saved = referralWithdrawalDetailsRepository.save(
      ReferralWithdrawalDetails(
        id = UUID.randomUUID(),
        referralId = referral.id,
        reasonCode = "Sentence expired",
        reasonDetails = null,
        createdAt = OffsetDateTime.now(),
        createdBy = referralUser.id,
      ),
    )

    assertThat(referralWithdrawalDetailsRepository.findById(saved.id)).isPresent
  }

  @Test
  fun `should reject withdrawal details when reason code does not match a known withdrawal reason name`() {
    val referralUser = referralHelper.ensureReferralUser()
    val referral = referralHelper.createReferral(submittedBy = referralUser)

    assertThrows<DataIntegrityViolationException> {
      referralWithdrawalDetailsRepository.saveAndFlush(
        ReferralWithdrawalDetails(
          id = UUID.randomUUID(),
          referralId = referral.id,
          reasonCode = "Not a real reason",
          reasonDetails = null,
          createdAt = OffsetDateTime.now(),
          createdBy = referralUser.id,
        ),
      )
    }
  }
}
