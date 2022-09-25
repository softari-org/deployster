package opstopus.deploptopus.test.github

import io.ktor.client.HttpClient
import io.ktor.client.engine.curl.Curl
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import opstopus.deploptopus.github.GitHubAuth
import opstopus.deploptopus.github.JWT
import opstopus.deploptopus.github.deploy.DeploymentStatus
import opstopus.deploptopus.github.events.InstallationPayload
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

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

            assertTrue(response.status.isSuccess(), response.status.toString())

            val body = response.bodyAsText()
            val installations = Json.decodeFromString<InstallationsPayload>(
                "{\"installations\":$body}"
            ).installations
            assertTrue(installations.isNotEmpty(), "Missing test installation")

            val testInstallation = installations.first()
            try {
                val testDeploymentStatus = DeploymentStatus(testInstallation)
                assertNotNull(testDeploymentStatus)
            } catch (e: NullPointerException) {
                fail(e.message)
            }
        }
    }

    companion object {
        val testClient = HttpClient(Curl) {
            this.defaultRequest {
                this.url {
                    host = "api.github.com"
                    protocol = URLProtocol.HTTPS
                }
                this.contentType(ContentType.Application.Json)
                this.accept(ContentType("application", "vnd.github+json"))
            }
            this.install(Logging)
            this.install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        @OptIn(ExperimentalSerializationApi::class)
                        explicitNulls = false
                    }
                )
            }
        }
    }
}
