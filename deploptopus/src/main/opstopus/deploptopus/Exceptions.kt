package opstopus.deploptopus

import io.ktor.http.HttpStatusCode

/**
 * Base exception for any exceptions which directly match to HTTP errors
 */
open class HttpException(override val message: String, val status: HttpStatusCode) :
    Exception(message)

/**
 * HTTP 400 Bad request
 */
class BadRequest(message: String) : HttpException(message, HttpStatusCode.BadRequest)
