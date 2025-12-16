package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "pdu")
class Pdu(
  @Id
  val id: UUID,

  @ManyToOne
  @JoinColumn(name = "contract_area_id", nullable = false)
  val contractArea: ContractArea,

  @Column(nullable = false)
  val name: String,
)
