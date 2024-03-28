package oopversion.infrastructure

import oopversion.domain.createfights.models.Fight
import oopversion.domain.createfights.models.FightLog
import oopversion.domain.createfights.models.Fighter
import oopversion.domain.createfights.models.LineType
import oopversion.domain.driven.AllFights
import oopversion.domain.models.*
import java.time.LocalTime

class InMemoryAllFights: AllFights {

    private val allFights = InMemoryFights(mutableSetOf())

    override fun save(fight: Fight) {
        allFights.fights.add(fight.toInfra())
    }

    override fun fetchAll(): Fights {
        return Fights(
            allFights.fights
                .map { it.toDomain() }
                .toMutableSet()
        )
    }

}

data class InMemoryFights(val fights: MutableSet<InMemoryFight>)

fun Fight.toInfra() = InMemoryFight(
    inMemoryId = id,
    inMemoryFighters = fighters.map { it.toInfra() }.toMutableSet(),
    inMemoryFightLogs = fightLogs.map { it.toInfra() }.toMutableList()
)

fun Fighter.toInfra() = InMemoryFighter(
    inMemoryId = id,
    inMemoryName = name,
    inMemoryIsAIControlled = isAIControlled
)

fun FightLog.toInfra() = InMemoryFightLog(
    inMemoryType = type.name,
    inMemoryTimeAt = timeAt,
    inMemoryIdentifier = identifier,
    inMemoryBody = body
)

data class InMemoryFight(
    val inMemoryId: Long,
    val inMemoryFighters: MutableSet<InMemoryFighter>,
    val inMemoryFightLogs: MutableList<InMemoryFightLog>
) {
    fun toDomain() = Fight(
        id = inMemoryId,
        fighters = inMemoryFighters.map { it.toDomain() }.toMutableSet(),
        fightLogs = inMemoryFightLogs.map { it.toDomain() }.toMutableList()
    )
}

data class InMemoryFighter(
    val inMemoryId: String,
    val inMemoryName: String,
    val inMemoryIsAIControlled: Boolean
) {
    fun toDomain() = Fighter(
        id = inMemoryId,
        name = inMemoryName,
        isAIControlled = inMemoryIsAIControlled
    )
}

data class InMemoryFightLog(
    val inMemoryType: String,
    val inMemoryTimeAt: LocalTime,
    val inMemoryIdentifier: String,
    val inMemoryBody: String
) {
    fun toDomain() = FightLog(
        type = LineType.valueOf(inMemoryType),
        timeAt = inMemoryTimeAt,
        identifier = inMemoryIdentifier,
        body = inMemoryBody
    )
}