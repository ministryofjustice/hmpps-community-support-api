package uk.gov.justice.digital.hmpps.communitysupportapi.service

import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AssignmentFailureDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CaseWorkerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.UserDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUserAssignment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toEntity
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AssignCaseWorkersResult
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CaseIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.validation.CaseIdentifierValidator
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import java.time.LocalDateTime
import java.util.UUID
import java.util.regex.Pattern

@Service
class ReferralAssignmentService(
  private val referralRepository: ReferralRepository,
  private val referralUserAssignmentRepository: ReferralUserAssignmentRepository,
  private val userService: UserService,
  private val entityManager: EntityManager,
) {
  companion object {
    private const val MAX_CASE_WORKERS = 5
    private val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    private val identifierValidator: CaseIdentifierValidator = CaseIdentifierValidator()
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getAssignedCaseWorkers(identifier: String): List<CaseWorkerDto>? {
    log.info("Get assigned case workers of a referral {}", identifier)

    val referral = when (val identifier = identifierValidator.validate(identifier)) {
      is CaseIdentifier.ReferralId -> referralRepository.findById(identifier.value)
        .orElseThrow { NotFoundException("Referral not found for id ${identifier.value}") }

      is CaseIdentifier.CaseId -> referralRepository.findByReferenceNumber(identifier.value).first()
    }

    val activeAssignments: List<ReferralUserAssignment> = referralUserAssignmentRepository.findAllByReferralIdAndNotDeleted(referral.id)

    return activeAssignments
      .map {
        CaseWorkerDto(
          if (it.user.authSource == AuthSource.AUTH.source) UserType.INTERNAL else UserType.EXTERNAL,
          userId = it.user.id,
          fullName = it.user.fullName,
          emailAddress = it.user.hmppsAuthUsername,
        )
      }
  }

  @Transactional
  fun assignCaseWorkers(assigner: ReferralUser, identifier: String, caseWorkers: List<CaseWorkerDto>): AssignCaseWorkersResult? {
    log.info("Assigning case workers to referral {}", identifier)

    val referral = when (val identifier = identifierValidator.validate(identifier)) {
      is CaseIdentifier.ReferralId -> referralRepository.findById(identifier.value)
        .orElseThrow { NotFoundException("Referral not found for id ${identifier.value}") }

      is CaseIdentifier.CaseId -> referralRepository.findByReferenceNumber(identifier.value).first()
    }

    inputValidation(caseWorkers)?.let { return it }

    val submittedAssignments = mutableListOf<Pair<String, UserDto>>()
    val failures = mutableListOf<AssignmentFailureDto>()

    val uniqueCaseworkers = caseWorkers.distinctBy { Pair(it.emailAddress, it.userId) }

    validateCaseWorkerByEmails(uniqueCaseworkers).forEach { (validation, user) ->
      if (validation.isValid) {
        user?.let {
          submittedAssignments += validation.emailAddress to it
          failures += AssignmentFailureDto(
            validation.emailAddress,
            validation.failureReason.orEmpty(),
          )
        }
      } else {
        failures += AssignmentFailureDto(
          validation.emailAddress,
          validation.failureReason.orEmpty(),
        )
      }
    }

    if (failures.any { it.reason.isNotEmpty() }) {
      return AssignCaseWorkersResult(
        success = false,
        message = "Failed to assign case worker(s)",
        failureList = failures,
      )
    }

    val allAssignments = referralUserAssignmentRepository.findAllByReferralId(referral.id)
    val existingAssignments = allAssignments.filter { it.deletedBy == null && it.deletedAt == null }

    val submittedIds = submittedAssignments.map { (_, user) -> user.id }.toSet()

    val now = LocalDateTime.now()

    if (failures.all { it.reason.isEmpty() }) {
      referralUserAssignmentRepository.deleteAllByReferralId(referral.id)
      entityManager.flush()

      submittedIds.forEach { userIdToAdd ->
        val user = submittedAssignments.first { it.second.id == userIdToAdd }.second
        referralUserAssignmentRepository.save(
          ReferralUserAssignment(UUID.randomUUID(), referral, user.toEntity(), now, assigner),
        )
      }

      val isModified = submittedAssignments.isNotEmpty() || existingAssignments.isNotEmpty()
      val isNotInProgress = existingAssignments.isEmpty()
      val isSingleCaseWorkerAssigned = submittedIds.size == 1
      val result = AssignCaseWorkersResult(
        success = true,
        message = when {
          isModified && isSingleCaseWorkerAssigned && !isNotInProgress -> "The caseworker assigned to this case has changed."
          isSingleCaseWorkerAssigned -> "The case has been assigned to a caseworker."
          isModified && !isNotInProgress -> "The caseworkers assigned to this case have changed."
          else -> "The case has been assigned to caseworkers."
        },
        succeededList = submittedAssignments.map { CaseWorkerDto(userType = if (it.second.authSource == AuthSource.AUTH.source) UserType.INTERNAL else UserType.EXTERNAL, userId = it.second.id, it.second.fullName, it.second.hmppsAuthUsername) },
      )
      log.info("Caseworkers assigned to referral {}: {}", identifier, result)
      return result
    } else {
      return AssignCaseWorkersResult(
        success = false,
        message = "Failed to assign case worker(s)",
        failureList = failures,
      )
    }
  }

  private data class EmailValidationResult(
    val emailAddress: String,
    val failureReason: String? = null,
  ) {
    val isValid: Boolean
      get() = failureReason.isNullOrBlank()

    val hasError: Boolean
      get() = !isValid
  }

  private fun validateEmailAddress(emailAddress: String): EmailValidationResult {
    val trimmedEmailAddress = emailAddress.trim().lowercase()

    return when {
      trimmedEmailAddress.isBlank() -> {
        EmailValidationResult(emailAddress, "Enter the caseworker's email address")
      }
      !emailPattern.matcher(trimmedEmailAddress).matches() -> {
        EmailValidationResult(emailAddress, "Enter an email address in the correct format, like name@example.com")
      }
      else -> {
        val user = userService.getUser(trimmedEmailAddress)
        if (user != null) {
          EmailValidationResult(emailAddress, null)
        } else {
          EmailValidationResult(emailAddress, "Could not find a caseworker with that email address.")
        }
      }
    }
  }

  private fun inputValidation(caseWorkers: List<CaseWorkerDto>): AssignCaseWorkersResult? {
    val uniqueEmailsCount = caseWorkers
      .map { it.emailAddress.trim().lowercase() }
      .filter { it.isNotBlank() }
      .distinct()

    if (uniqueEmailsCount.size > MAX_CASE_WORKERS) {
      return AssignCaseWorkersResult(
        success = false,
        message = "Cannot assign more than $MAX_CASE_WORKERS caseworkers.",
      )
    } else if (uniqueEmailsCount.isEmpty()) {
      return AssignCaseWorkersResult(
        success = false,
        message = "No email address provided.",
      )
    }
    return null
  }

  private fun validateCaseWorkerByEmails(caseWorkers: List<CaseWorkerDto>): List<Pair<EmailValidationResult, UserDto?>> {
    val results = mutableListOf<Pair<EmailValidationResult, UserDto?>>()

    for (caseWorker in caseWorkers) {
      val validResult = validateEmailAddress(caseWorker.emailAddress)
      if (validResult.isValid) {
        val user = userService.getUser(validResult.emailAddress)
        if (user != null) {
          results.add(validResult to user)
        } else {
          results.add(
            EmailValidationResult(
              caseWorker.emailAddress,
              "Could not find a caseworker with that email address.",
            ) to null,
          )
        }
      } else {
        results.add(validResult to null)
      }
    }
    return results
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
