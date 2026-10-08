package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataPdu
import java.util.UUID

interface PduRepository : JpaRepository<ReferenceDataPdu, UUID> {
  fun findByReferenceDataContractAreaId(contractAreaId: UUID): List<ReferenceDataPdu>

  fun findByName(name: String): ReferenceDataPdu?

  @Query("select p.name from ReferenceDataPdu p where p.id = :id")
  fun findNameById(id: UUID): String?
}
