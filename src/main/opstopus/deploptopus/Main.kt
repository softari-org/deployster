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
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import opstopus.deploptopus.github.Crypto
import opstopus.deploptopus.github.GitHubHeaders
import opstopus.deploptopus.github.events.DeploymentEventPayload
import opstopus.deploptopus.github.events.EventType
import opstopus.deploptopus.system.runner.Runner

internal fun Application.registerSerializers() {
    this.install(ContentNegotiation) {
        this.json()
    }
}

internal fun Application.registerExceptionHandlers() {
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

internal fun Routing.registerStatusEndpoint() {
    get("/") {
        this.call.respond(Status.get())
    }
}

internal fun Routing.registerWebhookEndpoint(config: Config) {
    post("/") {
        val eventName = this.call.request.header(
            GitHubHeaders.EVENT_TYPE.headerText
        )
        // If an event name is not provided, it's an invalid request
        if (eventName.isNullOrEmpty()) {
            throw BadRequest(
                "Header ${GitHubHeaders.EVENT_TYPE.headerText} required."
            )
        }

        /*
         * We get the type of the request from the event name header.
         * With that, we can fetch the corresponding data class which the
         * request payload will fit into, and then de-serialize the request.
         */
        val eventType: EventType
        val requestBody = this.call.receiveText()
        try {
            eventType = EventType[eventName]
            this.application.log.debug("Received payload $requestBody")
        } catch (e: NotFound) {
            this.call.application.log.error(
                "Received an unsupported webhook event"
            )
            throw e
        }

        // Decode the payload into the appropriate type
        val payload = when (eventType) {
            EventType.DEPLOYMENT -> Json.decodeFromString<DeploymentEventPayload>(requestBody)
            else -> throw NotFound("Unsupported event type $eventName")
        }
        val eventRepository = payload.repository.fullName

        val triggersToRun = config.triggers.filter {
            // Only run triggers for the incoming event on the incoming repository
            it.on.event == eventType && it.on.repository.lowercase() == eventRepository.lowercase()
        }

        // Verify that incoming request is signed with our secret
        if (!Crypto.signatureIsValid(
                requestBody,
                config.githubSecret,
                this.call.request.header(GitHubHeaders.HUB_SIGNATURE_SHA_256.headerText)
            )
        ) {
            // TODO update deployment status to error
            throw Forbidden("Request signature validation failed.")
        }

        // Execute runners
        val outputs = triggersToRun.map {
            this.application.log.info("Running $eventName trigger for $eventRepository.")
            return@map Runner.runRemote(
                it.user,
                it.host,
                it.port,
                it.key,
                it.command
            )
        }

        // Respond to the call with outputs from all the runners
        this.call.respond(
            EventResponse(
                triggersToRun.zip(outputs).map {
                    RunnerIO(it.first, it.second)
                }
            )
        )
    }
}

internal fun runServer(config: Config) {
    embeddedServer(CIO, host = "localhost", port = 8080) {
        this.log.debug("Running with config: ${Json.encodeToString(config)}")
        this.registerSerializers()
        this.registerExceptionHandlers()
        this.routing {
            this.trace { this.application.log.trace(it.buildText()) }
            this.registerStatusEndpoint()
            this.registerWebhookEndpoint(config)
        }
    }.start(wait = true)
}

fun main(args: Array<String>) {
    runServer(Config.loadConfiguration(args))
}
