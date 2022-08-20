package opstopus.deploptopus

import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(CIO, host = "localhost", port = 8080) {
        routing {
            trace { this.application.log.trace(it.buildText()) }
            get("/") {
                this.call.respondText("Hello!")
            }
        }
    }.start(wait = true)
}
