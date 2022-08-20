package opstopus.deploptopus.test

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.testing.testApplication
import opstopus.deploptopus.Status
import kotlin.test.Test
import kotlin.test.assertEquals

class StatusTest {
    @Test
    fun testStatus() = testApplication {
        val response = client.get("/")
        assertEquals(
            """{version:"deploptopus version ${Status.version}"}""",
            response.bodyAsText()
        )
    }
}
