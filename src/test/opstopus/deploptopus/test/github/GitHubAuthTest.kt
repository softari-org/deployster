package opstopus.deploptopus.test.github

import io.ktor.client.HttpClient
import io.ktor.client.engine.curl.Curl
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.append
import io.ktor.http.isSuccess
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import opstopus.deploptopus.github.GitHubAuth
import opstopus.deploptopus.github.JWT
import opstopus.deploptopus.github.deploy.DeploymentStatus
import opstopus.deploptopus.github.events.InstallationPayload
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@Serializable
internal data class InstallationsPayload(val installations: List<InstallationPayload>)

class GitHubAuthTest {
    @Test
    fun testJWT() {
        runBlocking {
            val response = GitHubAuthTest.testClient.get("/app/installations") {
                bearerAuth(JWT(GitHubAuth.GITHUB_APP_PRIVATE_KEY).jws)
            }

            assert(response.status.isSuccess()) { response.bodyAsText() }
        }
    }

    @Test
    fun testAccessToken() {
        runBlocking {
            val response = GitHubAuthTest.testClient.get("/app/installations") {
                bearerAuth(JWT(GitHubAuth.GITHUB_APP_PRIVATE_KEY).jws)
            }

            val body = response.bodyAsText()
            assert(response.status.isSuccess()) { body }

            val installations =
                Json.decodeFromString<InstallationsPayload>("{\"installations\":$body}")
            assert(installations.installations.isNotEmpty()) { "Missing test installation" }

            val testInstallation = installations.installations.first()
            try {
                val testDeploymentStatus = DeploymentStatus(testInstallation)
                assertNotNull(testDeploymentStatus)
            } catch (e: NullPointerException) {
                assertFalse(false, e.message)
            }
        }
    }

    companion object {
        private val testClient = HttpClient(Curl) {
            defaultRequest {
                url {
                    host = "api.github.com"
                    protocol = URLProtocol.HTTPS
                }
                headers {
                    append(HttpHeaders.ContentType, ContentType.Application.Json)
                    append(HttpHeaders.Accept, ContentType("application", "vnd.github+json"))
                }
            }
            install(Logging)
        }
    }
}
