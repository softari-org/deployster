package opstopus.deploptopus

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(CIO, host = "localhost", port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        routing {
            trace { this.application.log.trace(it.buildText()) }
            get("/") {
                this.call.respond(Status.get())
            }
        }
    }.start(wait = true)
}
