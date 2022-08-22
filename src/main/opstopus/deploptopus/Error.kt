package opstopus.deploptopus

import kotlinx.serialization.Serializable

/**
 * Standard response body for HTTP errors
 */
@Serializable
data class Error(val message: String)
