package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository

@Service
class CommunityServiceProviderService(
  private val communityServiceProviderRepository: CommunityServiceProviderRepository,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun communityServiceProviders() = communityServiceProviderRepository.findAll()
}
