package opstopus.deploptopus.test

import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.server.testing.testApplication
import opstopus.deploptopus.Status
import opstopus.deploptopus.serializersModule
import opstopus.deploptopus.statusModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StatusTest {
    @Test
    fun testStatus() = testApplication {
        application {
            this.serializersModule()
            this.statusModule()
        }
        val response = this.client.get("/") {
            accept(ContentType.Application.Json)
        }
        assertTrue(response.status.isSuccess(), message = response.status.toString())
        assertEquals(
            """{"version":"deploptopus version ${Status.version}"}""",
            response.bodyAsText()
        )
    }
}
