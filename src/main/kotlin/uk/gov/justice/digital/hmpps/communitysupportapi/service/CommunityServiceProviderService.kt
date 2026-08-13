package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CommunitySupportServiceDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CommunitySupportServicesDto
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PduRepository

@Service
class CommunityServiceProviderService(
  private val communityServiceProviderRepository: CommunityServiceProviderRepository,
  private val pduRepository: PduRepository,
) {
  fun communityServiceProviders(): CommunitySupportServicesDto {
    val providers = communityServiceProviderRepository.findAll()
    val services = providers.map { provider ->
      val pdus = pduRepository.findByContractAreaId(provider.contractArea.id)
        .map { it.name }
        .sorted()
      CommunitySupportServiceDto.from(provider, pdus)
    }
    return CommunitySupportServicesDto(communitySupportServices = services)
  }
}
