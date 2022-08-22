package opstopus.deploptopus

import kotlinx.serialization.Serializable

@Serializable
data class RunnerIO(val trigger: Trigger, val output: String)

@Serializable
data class EventResponse(val runners: List<RunnerIO>)
