package oopversion.domain

import oopversion.domain.driven.AllFights
import oopversion.domain.models.Fight
import oopversion.domain.models.FightLog
import oopversion.domain.models.Fighter
import oopversion.domain.models.LineType
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Goal here is to read the logs file and register the right lines in a FIGHT
// RULES
// line between "CREATION DU COMBAT" and "End fight"
// line with "(eyo:1364)" and "breed" -> usually together
// line with the pattern " XXXX [TIME] [Information (jeu)] || (Messages d'erreur)" or "(aTQ:174)"
// IF line is equal AND have LESS THAN 300 millis in time -> don't take the second one

// TODO: remove those ugly !!

// TODO: implement class ?
class CreateFights() {

}
fun createFights(allFights: AllFights, inputFile: File) {
    var currentFight = Fight(0, mutableSetOf(), mutableListOf())

    var inCombat = false

    var gatheringFighters = false

    val fighterNameRegex = Regex("fightId=\\d+\\s+([^=]+)\\s+breed")

    inputFile.forEachLine { line ->

        if (!inCombat && line.contains("(cfP:29)")) {
            currentFight = fetchFightId(line)
        }

        // TODO: replace "CREATION DU COMBAT" by "(bbL:46)" ?
        if (line.contains("CREATION DU COMBAT")) {
            inCombat = true
            gatheringFighters = true
        }

        // TODO: replace "[FIGHT] End fight" by "(baH:91)" ?
        if (line.contains("[FIGHT] End fight")) {
            inCombat = false

            allFights.save(currentFight)
            
            currentFight.clear()
        }

        when (inCombat) {
            true -> {
                // (ceE:51) is where all fighters are in combat regarding the logs
                if (line.contains("(ceE:51)")) {
                    gatheringFighters = false
                }

                if (gatheringFighters && line.contains(fighterNameRegex)) {
                    currentFight.fighters.add(generateFighter(line))
                }

                if (line.contains("(aTQ:174)")) {
                    val currentFightLine = generateFightLine(line)
                    if (!currentFight.fightLogs.any { it.isAlmostEqual(currentFightLine) }) {
                        currentFight.fightLogs.add(currentFightLine)
                    }

                }
            }

            false -> {}
        }
    }
}

private fun fetchFightId(line: String): Fight {
    val fightIdRegex = Regex("id\\s(\\d+)\\s+has")
    val fightIdMatch = fightIdRegex.find(line)
    val fightId = fightIdMatch?.groupValues?.get(1)?.toInt()

    return Fight(id = fightId!!, fighters = mutableSetOf(), fightLogs = mutableListOf())
}

private fun generateFighter(line: String): Fighter {
    val idRegex = Regex("""\[(-?\d+)]""")
    val nameRegex = Regex("fightId=\\d+\\s+([^=]+)\\s+breed")
    val isControlledRegex = Regex("isControlledByAI=(\\w+)")

    val idMatch = idRegex.find(line)
    val nameMatch = nameRegex.find(line)
    val isControlledMatch = isControlledRegex.find(line)

    val id = idMatch?.groupValues?.get(1)
    val name = nameMatch?.groupValues?.get(1)
    val isControlled = isControlledMatch?.groupValues?.get(1)?.toBoolean()

    return Fighter(id!!, name!!, isControlled!!)
}

private fun generateFightLine(line: String): FightLog {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS")

    val typeRegex = Regex("(\\w{4})\\s")
    val timeRegex = Regex("\\d{2}:\\d{2}:\\d{2},\\d{3}")
    val infoRegex = Regex("\\[(Information \\(jeu\\)|Messages d'erreur)]\\s*(.+)")

    val typeMatch = typeRegex.find(line)
    val timeMatch = timeRegex.find(line)
    val infoMatch = infoRegex.find(line)

    val type = typeMatch?.groupValues?.get(1)
    val time = timeMatch?.value?.replace(",", ":")
    val body = infoMatch?.groupValues?.get(2)?.trim()

    return FightLog(
        type = LineType.valueOf(type!!),
        timeAt = LocalTime.parse(time, formatter),
        identifier = "(aTQ:174)",
        body = body!!
    )
}