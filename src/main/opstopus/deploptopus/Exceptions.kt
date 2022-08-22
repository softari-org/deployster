package opstopus.deploptopus

import io.ktor.http.HttpStatusCode

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
