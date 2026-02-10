package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AssignmentFailureDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CaseWorkerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.UserDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUserAssignment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toEntity
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AssignCaseWorkersResult
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.service.UserService
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import java.time.LocalDateTime
import java.util.UUID
import java.util.regex.Pattern

@Service
class ReferralAssignmentService(
  private val referralUserRepository: ReferralUserRepository,
  private val referralRepository: ReferralRepository,
  private val referralUserAssignmentRepository: ReferralUserAssignmentRepository,
  private val userService: UserService,
) {
  companion object {
    private const val MAX_CASE_WORKERS = 5
    private val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun assignCaseWorkers(assigner: ReferralUser, referralId: UUID, caseWorkers: List<CaseWorkerDto>): AssignCaseWorkersResult? {
    log.info("Assigning case workers to referral {}", referralId)

    val referral = referralRepository.findById(referralId)
      .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Referral not found") }

    val uniqueEmailsCount = caseWorkers
      .map { it.emailAddress.trim().lowercase() }
      .filter { it.isNotBlank() }
      .distinct()

    if (uniqueEmailsCount.size > MAX_CASE_WORKERS) {
      return AssignCaseWorkersResult(
        success = false,
        message = "Cannot assign more than $MAX_CASE_WORKERS emails.",
      )
    }

    val submittedAssignments = mutableListOf<Pair<String, UserDto>>()
    val failures = mutableListOf<AssignmentFailureDto>()

    for (caseWorker in caseWorkers) {
      if (caseWorker.emailAddress.isBlank()) {
        failures += AssignmentFailureDto(caseWorker.emailAddress, "Enter the caseworker's email address")
      }
      if (!emailPattern.matcher(caseWorker.emailAddress).matches()) {
        failures += AssignmentFailureDto(caseWorker.emailAddress, "Enter an email address in the correct format, like name@example.com")
      }
      val user = userService.getUser(caseWorker.emailAddress)
      if (user == null) {
        failures += AssignmentFailureDto(caseWorker.emailAddress, "Could not find a caseworker with that email address.")
      } else {
        submittedAssignments += caseWorker.emailAddress to user
      }
    }
    val allAssignments = referralUserAssignmentRepository.findAllByReferralId(referral.id)
    val existingAssignments = allAssignments.filter { it.deletedBy != null && it.deletedAt != null }
    val deletedAssignments = allAssignments.filter { it.deletedBy == null || it.deletedAt == null }

    val submittedIds = submittedAssignments.map { (email, user) -> user.id }.toSet()
    val existingIds = existingAssignments.map { it.id }.toSet()
    val deletedIds = deletedAssignments.map { it.id }.toSet()

    val toUpdate = submittedIds intersect deletedIds
    val toAdd = submittedIds - existingIds - deletedIds
    val toRemove = existingIds - toAdd

    val now = LocalDateTime.now()
    val savedAssignments: List<Pair<String, UserDto>>?
    var removedAssignments: List<Pair<String, UserDto>>?

    if (failures.isEmpty()) {
      if (toAdd.isNotEmpty()) {
        savedAssignments = submittedAssignments
          .filter { it.second.id in toAdd }
          .map { (email, user) ->
            val assignmentId = UUID.randomUUID()
            val assignment = ReferralUserAssignment(assignmentId, referral, user.toEntity(), now, assigner)
            referralUserAssignmentRepository.save(assignment)
            email to user
          }
      }
      if (toUpdate.isNotEmpty()) {
        existingAssignments
          .filter { it.id in toUpdate }
          .forEach { assignment ->
            referralUserAssignmentRepository.updateByReferralIdAndUserId(referral.id, assignment.user.id, assigner.id, now)
          }
      }
      if (toRemove.isNotEmpty()) {
        existingAssignments
          .filter { it.id in toRemove }
          .forEach { assignment ->
            referralUserAssignmentRepository.markDeletedByReferralIdAndUserId(referral.id, assignment.user.id, assigner.id, now)
          }
      }
      val message: String = if (toUpdate.isNotEmpty()) {
        if (submittedAssignments.size == 1) {
          "The caseworker assigned to this case has changed."
        } else {
          "The caseworkers assigned to this case have changed."
        }
      } else {
        if (submittedAssignments.size == 1) {
          "The case has been assigned to a caseworker."
        } else {
          "The case has been assigned to caseworkers."
        }
      }
      val result: AssignCaseWorkersResult = AssignCaseWorkersResult(
        success = true,
        message = message,
        succeededList = submittedAssignments.map { CaseWorkerDto(userType = if (it.second.authSource == AuthSource.AUTH.source) UserType.INTERNAL else UserType.EXTERNAL, userId = it.second.id, it.second.fullName, it.second.hmppsAuthUsername) },
      )
      return result
    } else {
      val result: AssignCaseWorkersResult = AssignCaseWorkersResult(
        success = false,
        message = "Failed to assign case worker(s)",
        failureList = failures,
      )
      return result
    }
  }

  fun recentlySynchronised(user: ReferralUser): Boolean {
    val lastSync = user.lastSyncedAt
    if (lastSync == null) {
      return false
    } else {
      return lastSync.isAfter(LocalDateTime.now().minusMinutes(1))
    }
  }
}
