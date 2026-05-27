package uk.gov.justice.digital.hmpps.communitysupportapi.exception

class NotFoundException(message: String) : RuntimeException(message)

class ConflictException(message: String) : RuntimeException(message)
