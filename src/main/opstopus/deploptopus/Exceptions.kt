package opstopus.deploptopus

import io.ktor.http.HttpStatusCode

/**
 * Exception for a required secret which was not provided
 */
class MissingSecretException(override val message: String? = null) : Exception()

/**
 * Exception for errors occurring with C memory allocation
 */
class MemoryAllocationException : Exception()

/**
 * Base exception for any exceptions which directly match to HTTP errors
 */
sealed class HttpException(override val message: String, val status: HttpStatusCode) :
    Exception(message)

/**
 * HTTP 400 Bad request
 */
class BadRequest(message: String) : HttpException(message, HttpStatusCode.BadRequest)

/**
 * HTTP 403 Forbidden
 */
class Forbidden(message: String) : HttpException(message, HttpStatusCode.Forbidden)

/**
 * HTTP 404 Not found
 */
class NotFound(message: String) : HttpException(message, HttpStatusCode.NotFound)

/**
 * HTTP 500 Internal server error
 */
class InternalServerError(message: String) : HttpException(
    message,
    HttpStatusCode.InternalServerError
)
