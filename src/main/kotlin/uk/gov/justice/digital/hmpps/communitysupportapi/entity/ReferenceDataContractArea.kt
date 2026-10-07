package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "reference_data_contract_area")
class ReferenceDataContractArea(
  @Id
  val id: UUID,

  @ManyToOne
  @JoinColumn(name = "region_id", nullable = false)
  val referenceDataRegion: ReferenceDataRegion,

  @Column(name = "area", nullable = false)
  val area: String,
)
