package oopversion.domain.createfights.models

import java.time.LocalTime
import kotlin.math.abs

data class FightLog(
    val type: LineType,
    val timeAt: LocalTime,
    // TODO: is it useful ?
    val identifier: String,
    val body: String
) {

    fun isAlmostEqual(other: FightLog): Boolean {
        return abs(timeAt.toNanoOfDay() - other.timeAt.toNanoOfDay()) < 300_000_000
                && type == other.type
                && identifier == other.identifier
                && body == other.body
    }
}

enum class LineType {
    INFO,
    WARN
}
