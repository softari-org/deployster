package opstopus.deploptopus

import kotlinx.serialization.Serializable
import opstopus.deploptopus.system.runner.RunnerIO

@Serializable
data class EventResponse(val runners: List<RunnerIO>)
