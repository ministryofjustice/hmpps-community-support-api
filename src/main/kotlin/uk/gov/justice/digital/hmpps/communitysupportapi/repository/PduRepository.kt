package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Pdu
import java.util.UUID

interface PduRepository : JpaRepository<Pdu, UUID> {
  fun findByContractAreaId(contractAreaId: UUID): List<Pdu>
}
