package opstopus.deploptopus.test.github

import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.server.testing.testApplication
import kotlinx.cinterop.toKString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import opstopus.deploptopus.Config
import opstopus.deploptopus.MissingSecretException
import opstopus.deploptopus.RepositoryEvent
import opstopus.deploptopus.Trigger
import opstopus.deploptopus.github.Crypto
import opstopus.deploptopus.github.GitHubAuth
import opstopus.deploptopus.github.GitHubHeaders
import opstopus.deploptopus.github.JWT
import opstopus.deploptopus.github.events.DeploymentEventPayload
import opstopus.deploptopus.github.events.DeploymentPayload
import opstopus.deploptopus.github.events.GitHubAccessTokenPayload
import opstopus.deploptopus.github.events.InstallationPayload
import opstopus.deploptopus.github.events.PartialInstallationPayload
import opstopus.deploptopus.github.events.RepositoryPayload
import opstopus.deploptopus.serializersModule
import opstopus.deploptopus.webhookModule
import platform.posix.getenv
import kotlin.test.Test
import kotlin.test.assertTrue

@Serializable
internal data class CreateDeploymentRequestPayload(val ref: String, val environment: String)

@Serializable
internal data class ListDeploymentsResponsePayload(val deployments: List<DeploymentPayload>)

private val testConfig = Config(
    triggers = listOf(
        Trigger(
            on = RepositoryEvent(
                repository = getenv("WEBHOOK_TEST_REPO")?.toKString()
                    ?: throw MissingSecretException()
            ),
            command = getenv("WEBHOOK_TEST_COMMAND")?.toKString() ?: "hostname",
            user = getenv("WEBHOOK_TEST_USER")?.toKString() ?: throw MissingSecretException(),
            host = getenv("WEBHOOK_TEST_HOST")?.toKString() ?: throw MissingSecretException(),
            port = getenv("WEBHOOK_TEST_PORT")?.toKString()?.toUInt() ?: 22u,
            key = getenv("WEBHOOK_TEST_KEY")?.toKString() ?: throw MissingSecretException()
        )
    ),
    githubWebhookSecret = "wowimmasecret!"
)

private val formatter = Json {
    ignoreUnknownKeys = true
    @OptIn(ExperimentalSerializationApi::class)
    explicitNulls = false
}

class WebhookTest {
    @Test
    @Suppress("LongMethod")
    fun testWebhook() = testApplication {
        application {
            this.serializersModule()
            this.webhookModule(testConfig)
        }

        val testEnv = getenv("WEBHOOK_TEST_ENVIRONMENT")?.toKString() ?: "Testing"
        val testToken = JWT(GitHubAuth.GITHUB_APP_PRIVATE_KEY).jws
        val testRef = getenv("WEBHOOK_TEST_REF")?.toKString() ?: "main"
        val testInstallationID =
            getenv("WEBHOOK_TEST_INSTALLATION_ID")?.toKString() ?: throw MissingSecretException()

        val accessTokenResponse =
            GitHubAuthTest.testClient.post("/app/installations/$testInstallationID/access_tokens") {
                bearerAuth(testToken)
            }

        assertTrue(
            accessTokenResponse.status.isSuccess(),
            "Failed to get access token: ${accessTokenResponse.status}"
        )

        val accessToken = accessTokenResponse.body<GitHubAccessTokenPayload>().token

        val deploymentURL = "/repos/${testConfig.triggers[0].on.repository}/deployments"

        val listDeploymentsResponse = GitHubAuthTest.testClient.get(deploymentURL) {
            bearerAuth(accessToken)
        }

        assertTrue(
            listDeploymentsResponse.status.isSuccess(),
            "Failed to list deployments: ${listDeploymentsResponse.status}"
        )

        val deployments = formatter.decodeFromString<ListDeploymentsResponsePayload>(
            "{\"deployments\":${listDeploymentsResponse.bodyAsText()}}"
        )

        val testDeployment = deployments.deployments.firstOrNull {
            it.ref == testRef && it.environment == testEnv
        } ?: run {
            val createDeploymentResponse = GitHubAuthTest.testClient.post(
                deploymentURL
            ) {
                bearerAuth(accessToken)
                setBody(CreateDeploymentRequestPayload(ref = testRef, environment = testEnv))
            }

            assertTrue(
                createDeploymentResponse.status.isSuccess(),
                "Failed to create test deployment: ${createDeploymentResponse.status}"
            )
            return@run createDeploymentResponse.body<DeploymentPayload>()
        }

        val getRepoResponse = GitHubAuthTest.testClient.get(testDeployment.repositoryURL) {
            bearerAuth(accessToken)
        }

        assertTrue(getRepoResponse.status.isSuccess(), getRepoResponse.status.toString())

        val testRepo = getRepoResponse.body<RepositoryPayload>()

        val getInstallationResponse =
            GitHubAuthTest.testClient.get("/app/installations/$testInstallationID") {
                bearerAuth(testToken)
            }

        assertTrue(
            getInstallationResponse.status.isSuccess(),
            "Failed to get installation: ${getInstallationResponse.status}"
        )
        val testInstallation = getInstallationResponse.body<InstallationPayload>()

        val testBody = DeploymentEventPayload(
            action = "",
            deployment = DeploymentPayload(
                url = testDeployment.url,
                id = testDeployment.id,
                nodeID = testDeployment.nodeID,
                sha = testDeployment.sha,
                ref = testDeployment.ref,
                task = testDeployment.task,
                payload = testDeployment.payload,
                environment = testDeployment.environment,
                description = testDeployment.description,
                creator = testDeployment.creator,
                createdAt = testDeployment.createdAt,
                updatedAt = testDeployment.updatedAt,
                statusesURL = testDeployment.statusesURL,
                repositoryURL = testDeployment.repositoryURL
            ),
            repository = testRepo,
            sender = testDeployment.creator!!,
            installation = PartialInstallationPayload(testInstallation.id)
        )
        val testBodyText = formatter.encodeToString(testBody)
        val testBodySignature = Crypto.signHMACSHA256(
            testBodyText,
            testConfig.githubWebhookSecret
        )

        val response = this.client.post("/") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            header(GitHubHeaders.HUB_SIGNATURE_SHA_256.headerText, testBodySignature)
            setBody(testBodyText)
        }

        assertTrue(response.status.isSuccess(), response.status.toString())
    }
}
