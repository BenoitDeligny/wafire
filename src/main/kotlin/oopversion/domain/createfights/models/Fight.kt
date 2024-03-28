package oopversion.domain.createfights.models

import oopversion.domain.createfights.models.FightLog
import oopversion.domain.createfights.models.Fighter

data class Fight(
    val id: Long,
    val fighters: MutableSet<Fighter>,
    val fightLogs: MutableList<FightLog>
) {
    fun clear() = this.copy(id = 0, fighters = mutableSetOf(), fightLogs = mutableListOf())
}