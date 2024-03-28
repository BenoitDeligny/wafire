package oopversion.domain.analyzefights.models

data class CompleteFighter(
    val id: String,
    val name: String,
    val fightStatistics: FightStatistics,
    val isAIControlled: Boolean,
    val summons: MutableSet<Summons>,
    var takeTurn: Boolean = false
) {
    fun startTurn() {
        takeTurn = true
    }

    fun endTurn() {
        takeTurn = false
    }

    fun addDamages(damagesToAdd: Int) {
        fightStatistics.damages += damagesToAdd
    }

    fun addHeals(healsToAdd: Int) {
        fightStatistics.heals += healsToAdd
    }

    fun addShields(shieldsToAdd: Int) {
        fightStatistics.shields += shieldsToAdd
    }
}

data class FightStatistics(
    var damages: Int = 0,
    var heals: Int = 0,
    var shields: Int = 0,
    val states: AppliedStates = AppliedStates(mutableSetOf())
)

data class AppliedStates(
    val name: MutableSet<String>
)

data class Summons(
    val name: String,
    val fightStatistics: FightStatistics = FightStatistics()
)
