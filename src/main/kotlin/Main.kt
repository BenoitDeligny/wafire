import oopversion.domain.analyzefights.analyzeFights
import oopversion.domain.createfights.createFights
import oopversion.infrastructure.InMemoryAllFights
import java.io.File

fun main() {
    val allFights = InMemoryAllFights()

    val inputFile = File("src/main/resources/wakfu.log")
    val outputFile = File("src/main/kotlin/damagesLog/wakfuLogs")

    createFights(allFights, inputFile)
    analyzeFights(allFights)

    var inCombat = false
    var currentFighter = ""
    val fighters = mutableListOf<String>()

    val fightersDamages = mutableMapOf<String, Int>()
    val fightersHeals = mutableMapOf<String, Int>()
    val fightersShields = mutableMapOf<String, Int>()

    val fighterNameRegex = Regex("fightId=\\d+\\s+([^=]+)\\s+breed")
    val damagesRegex = Regex("""-(\d+)\sPV""")
    val healsRegex = Regex("""\+(\d+)\sPV""")
    val shieldsRegex = Regex(""": (\d+)\sArmure""")

    inputFile.forEachLine { line ->
        if (line.contains("CREATION DU COMBAT")) {
            inCombat = true
        } else if (inCombat && line.contains(fighterNameRegex)) {
            val result = fighterNameRegex.find(line)
            fighters.add(result?.groupValues?.get(1).toString())
        } else if (line.contains("[FIGHT] End fight")) {
            inCombat = false

            outputFile.appendText("\nFIGHT:  \n")
            outputFile.appendText("\n--- DAMAGES ---\n")

            val fightersDamagesOrdered = fightersDamages.toList().sortedByDescending { (_, value) -> value }.toMap()
            fightersDamagesOrdered.forEach {
                if (it.value > 0) {
                    outputFile.appendText("${it.key} : ${it.value}\n")
                }
            }
            outputFile.appendText("\n--- HEALS ---\n")
            val fightersHealsOrdered = fightersHeals.toList().sortedByDescending { (_, value) -> value }.toMap()
            fightersHealsOrdered.forEach {
                if (it.value > 0) {
                    outputFile.appendText("${it.key} : ${it.value}\n")
                }
            }

            outputFile.appendText("\n--- SHIELDS ---\n")
            val fightersShieldsOrdered = fightersShields.toList().sortedByDescending { (_, value) -> value }.toMap()
            fightersShieldsOrdered.forEach {
                if (it.value > 0) {
                    outputFile.appendText("${it.key} : ${it.value}\n")
                }
            }

            fighters.clear()
            fightersDamages.clear()
            fightersHeals.clear()
            fightersShields.clear()

        } else if (inCombat && line.contains("lance le sort")) {
            val fighter = line.substringAfter("[Information (jeu)]").substringBefore(" lance le sort").trim()
            if (fighter != currentFighter) {
                currentFighter = fighter
            }
        } else if (inCombat && line.contains(damagesRegex)) {
            val damages = line.substringAfterLast("-").trim().substringBefore(" PV").toIntOrNull() ?: 0
            fightersDamages[currentFighter] = fightersDamages.getOrDefault(currentFighter, 0) + (damages)

        } else if (inCombat && line.contains(healsRegex)) {
            val heals = line.substringAfterLast("+").trim().substringBefore(" PV").toIntOrNull() ?: 0
            fightersHeals[currentFighter] = fightersHeals.getOrDefault(currentFighter, 0) + (heals)

        } else if (inCombat && line.contains(shieldsRegex)) {
            val shields = line.substringAfterLast(":").trim().substringBefore(" Armure").toIntOrNull() ?: 0
            fightersShields[currentFighter] = fightersShields.getOrDefault(currentFighter, 0) + (shields)

        }
    }
}
