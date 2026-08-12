package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CommunitySupportServiceDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PageResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PduRepository

@Service
class CommunityServiceProviderService(
  private val communityServiceProviderRepository: CommunityServiceProviderRepository,
  private val pduRepository: PduRepository,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun communityServiceProviders(pageable: Pageable): PageResponse<CommunitySupportServiceDto> {
    val providers = communityServiceProviderRepository.findAll(pageable)
    val services = providers.map { provider ->
      val pdus = pduRepository.findByContractAreaId(provider.contractArea.id)
        .map { it.name }
        .sorted()
      CommunitySupportServiceDto.from(provider, pdus)
    }
    return services.toResponse()
  }
}
