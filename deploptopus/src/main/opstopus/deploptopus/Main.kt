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
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(CIO, host = "localhost", port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        install(StatusPages) {
            exception<Throwable> { call, exc ->
                call.application.log.error("Unhandled error", exc)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    Error("Unknown internal server error")
                )
            }
            exception<HttpException> { call, exc ->
                call.respond(exc.status, Error(exc.message))
            }
        }
        routing {
            trace { this.application.log.trace(it.buildText()) }
            get("/") {
                this.call.respond(Status.get())
            }
        }
    }.start(wait = true)
}
