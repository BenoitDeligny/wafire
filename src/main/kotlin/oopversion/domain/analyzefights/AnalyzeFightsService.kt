package oopversion.domain.analyzefights

import oopversion.domain.analyzefights.models.*
import oopversion.domain.createfights.models.FightLog
import oopversion.domain.createfights.models.Fighter
import oopversion.domain.driven.AllFights

// Goal here is to read the logs file and register the right lines in a FIGHT
// RULES
//

// Check behaviour on damages when armor (0 in logs or real damages ?) ->

// Ligne dégats avec état -XX PV (ELEMENT) (NOM DE L'ETAT)
// Total damages includes self-damages -> si contient damageRegex && fighterNameRegex = currentFighter
// Friendly fire ? -> check les noms des perso (si distinction faite entre player et mob)
// How to handle poison/async damages-heal ->
// check after "lance le sort" ->  pseudo: ETAT (+XX Niv.)
// attribuer ETAT au currentFighter
// check quand damage-heal regex avec ETAT dans la ligne

// bug on heals -> when sadida invoke tree and tree levelup -> heals : should not enter in calculation

// TODO: implement class ?

fun analyzeFights(allFights: AllFights) {

    val damagesRegex = Regex("""-(\d+)\sPV""")
    val healsRegex = Regex("""\+(\d+)\sPV""")
    val shieldsRegex = Regex(""": (\d+)\sArmure""")

    allFights.fetchAll().fights.forEach { fight ->
        // TODO: better to create the fight at the end and put fighters in after calculation ?
        val analyzedFight = AnalyzedFight(
            id = fight.id,
            fight.fighters.map { it.toCompleteFighter() }.toMutableSet()
        )

        fight.fightLogs.forEach { log ->
            when {
                log.body.contains("lance le sort") -> {
                    stopPreviousFighterTurn(analyzedFight.completeFighters)

                    val fighter = log.body
                        .substringAfter("[Information (jeu)]")
                        .substringBefore(" lance le sort")
                        .trim()

                    analyzedFight.completeFighters
                        .find { it.name == fighter }
                        ?.apply { startTurn() }
                        ?: analyzedFight.completeFighters.flatMap { it.summons }
                            .find { it.name == fighter }
                            ?.let { summon -> analyzedFight.completeFighters.find { it.summons.contains(summon) } }
                            ?.apply { startTurn() }
                }

                log.body.contains("Invoque un(e)") -> {
                    actualFighter(analyzedFight.completeFighters)
                        .summons.add(Summons(name = log.body.substringAfter("Invoque un(e) ")))
                }

                log.body.contains(damagesRegex) -> {
                    val damages = log.body.substringAfterLast("-").trim().substringBefore(" PV").toIntOrNull() ?: 0
                    if (areNotSelfDamages(log, analyzedFight)) {
                        actualFighter(analyzedFight.completeFighters).addDamages(damages)
                    }
                    // TODO: do not add friendly fire damages
                    // TODO: add fightStat to summons ?
                }

                log.body.contains(healsRegex) -> {
                    val heals = log.body.substringAfterLast("+").trim().substringBefore(" PV").toIntOrNull() ?: 0
                    actualFighter(analyzedFight.completeFighters).addHeals(heals)
                }

                log.body.contains(shieldsRegex) -> {
                    val shields = log.body.substringAfterLast(":").trim().substringBefore(" Armure").toIntOrNull() ?: 0
                    actualFighter(analyzedFight.completeFighters).addShields(shields)
                }
            }
        }
    }
}

private fun Fighter.toCompleteFighter() = CompleteFighter(
    id = id,
    name = name,
    fightStatistics = FightStatistics(
        damages = 0,
        heals = 0,
        shields = 0,
        states = AppliedStates(mutableSetOf())
    ),
    summons = mutableSetOf()
)

private fun stopPreviousFighterTurn(completeFighters: MutableSet<CompleteFighter>) {
    completeFighters.forEach { it.endTurn() }
}

private fun actualFighter(completeFighters: MutableSet<CompleteFighter>) = completeFighters.find { it.takeTurn }!!

private fun areNotSelfDamages(log: FightLog, analyzedFight: AnalyzedFight): Boolean {
    return log.body.substringAfter("[Information (jeu)] ").substringBefore(": -") != actualFighter(analyzedFight.completeFighters).name
}
