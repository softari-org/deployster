package opstopus.deploptopus

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import opstopus.deploptopus.github.GitHubHeaders
import opstopus.deploptopus.github.events.EventType

fun main() {
    embeddedServer(CIO, host = "localhost", port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        install(StatusPages) {
            /*
             * This is the base exception handler for otherwise unhandled exceptions.
             * It must remain at the top of this plugin declaration because of
             * weird rules that are used to resolve the right handler
             */
            exception<Throwable> { call, exc ->
                call.application.log.error("Unhandled error", exc)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    Error("Unknown internal server error")
                )
            }
            // Resolve manually thrown HTTP errors
            exception<HttpException> { call, exc ->
                call.respond(exc.status, Error(exc.message))
            }
        }
        routing {
            trace { this.application.log.trace(it.buildText()) }
            get("/") {
                this.call.respond(Status.get())
            }
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

                // We get the type of the request from the event name header.
                // With that, we can fetch the corresponding data class which the
                // request payload will fit into, and then de-serialize the request.
                val payload = this.call.receive(EventType[eventName].payloadType)
            }
        }
    }.start(wait = true)
}
