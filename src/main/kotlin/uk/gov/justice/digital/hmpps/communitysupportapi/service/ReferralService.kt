package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.util.Optional
import java.util.UUID

@Service
@Transactional
class ReferralService(
  private val referralRepository: ReferralRepository,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getReferral(referralId: UUID): Optional<Referral> = referralRepository.findById(referralId)
}
