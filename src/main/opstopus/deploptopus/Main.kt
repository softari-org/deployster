package opstopus.deploptopus

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.header
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import opstopus.deploptopus.github.Crypto
import opstopus.deploptopus.github.GitHubHeaders
import opstopus.deploptopus.github.deploy.DeploymentState
import opstopus.deploptopus.github.deploy.DeploymentStatus
import opstopus.deploptopus.github.events.DeploymentEventPayload
import opstopus.deploptopus.system.runner.Runner
import platform.posix.EXIT_SUCCESS

internal fun Application.serializersModule() {
    this.install(ContentNegotiation) {
        this.json()
    }
}

internal fun Application.exceptionHandlersModule() {
    this.install(StatusPages) {
        /*
         * This is the base exception handler for otherwise unhandled exceptions.
         * It must remain at the top of this plugin declaration because of
         * weird rules that are used to resolve the right handler
         */
        this.exception<Throwable> { call, exc ->
            call.application.log.error("Unhandled error", exc)
            call.respond(
                HttpStatusCode.InternalServerError,
                Error("Unknown internal server error")
            )
        }
        // Resolve manually thrown HTTP errors
        this.exception<HttpException> { call, exc ->
            call.respond(exc.status, Error(exc.message))
        }
    }
}

internal fun Application.statusModule() {
    routing {
        get("/") {
            this.call.respond(Status.get())
        }
    }
}

internal fun Application.webhookModule(config: Config) {
    val formatter = Json {
        ignoreUnknownKeys = true
        @OptIn(ExperimentalSerializationApi::class)
        explicitNulls = false
    }
    routing {
        post("/") {
            val requestBody = this.call.receiveText()

            // Decode the payload into the appropriate type
            val payload = try {
                formatter.decodeFromString<DeploymentEventPayload>(requestBody)
            } catch (e: SerializationException) {
                this.application.log.error(e.toString())
                throw BadRequest("Unsupported event.")
            }

            val eventRepository = payload.repository.fullName
            val installation = payload.installation ?: throw BadRequest("No installation provided")
            val deploymentStatus = DeploymentStatus(installation)

            deploymentStatus.update(payload.deployment, DeploymentState.PENDING)

            val triggersToRun = config.triggers.filter {
                // Only run triggers for the incoming event on the incoming repository
                it.on.repository.lowercase() == eventRepository.lowercase()
            }

            // Verify that incoming request is signed with our secret
            if (!Crypto.signatureIsValid(
                    requestBody,
                    config.githubWebhookSecret,
                    this.call.request.header(GitHubHeaders.HUB_SIGNATURE_SHA_256.headerText)
                )
            ) {
                deploymentStatus.update(payload.deployment, DeploymentState.ERROR)
                throw Forbidden("Request signature validation failed.")
            }

            // Execute runners
            val outputs = triggersToRun.map {
                this.application.log.info("Running deployment trigger for $eventRepository.")
                Runner.runRemote(
                    it.user,
                    it.host,
                    it.port,
                    it.key,
                    it.command
                ).let { io ->
                    if (io.status != EXIT_SUCCESS) {
                        this.application.log.error("Runner failed with exit code ${io.status}.")
                    } else {
                        this.application.log.info("Runner succeeded.")
                    }
                    this.application.log.info(io.output)
                    return@map io
                }
            }

            if (outputs.all { it.status == EXIT_SUCCESS }) {
                deploymentStatus.update(payload.deployment, DeploymentState.SUCCESS)
            } else {
                deploymentStatus.update(payload.deployment, DeploymentState.FAILURE)
            }

            // Respond to the call with outputs from all the runners
            this.call.respond(EventResponse(outputs))
        }
    }
}

internal fun runServer(config: Config) {
    embeddedServer(CIO, host = "0.0.0.0", port = 8080) {
        this.log.debug("Running with config: ${Json.encodeToString(config)}")
        this.serializersModule()
        this.exceptionHandlersModule()
        this.routing {
            this.trace { this.application.log.trace(it.buildText()) }
        }
        this.statusModule()
        this.webhookModule(config)
    }.start(wait = true)
    GlobalLifecycle.end()
}

fun main(args: Array<String>) {
    runServer(Config.loadConfiguration(args))
}
