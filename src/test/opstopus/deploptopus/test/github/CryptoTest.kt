package opstopus.deploptopus.test.github

import io.ktor.server.testing.testApplication
import opstopus.deploptopus.github.Crypto
import kotlin.test.Test
import kotlin.test.assertEquals

class CryptoTest {
    @Test
    fun testComputeSignature() = testApplication {
        val payload = "foo"
        val secret = "secret!"
        val actualSignature =
            "sha256=04c366ed28f695a1f3477765f3c98073d2fc62a0192e0811d1cda57295dcec6e"

        assertEquals(actualSignature, Crypto.computeSignature(payload, secret))
    }
}
