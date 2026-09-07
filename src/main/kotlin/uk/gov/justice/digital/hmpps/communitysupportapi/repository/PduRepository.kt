package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Pdu
import java.util.UUID

interface PduRepository : JpaRepository<Pdu, UUID> {
  fun findByContractAreaId(contractAreaId: UUID): List<Pdu>

  @Query("select p.name from Pdu p where p.id = :id")
  fun findNameById(id: UUID): String?
}
