package functionalversion

import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


// Make each part a function
// Should I switch to OOP ?
// Mutli account = twice the result (even on wakfu log -> seems sometimes yes, sometimes not)
// Check behaviour on damages when armor (0 in logs or real damages ?) ->

// How to handle invocation ? -> "pseudo: Invoque un(e) nom invocation" -> attribuer les dégats de l'invocation (OOP)
// How to distinct player from mob -> lister les "breed" en début de combat et check if isControlledByAI=true/false
// How distinct invocation then ? ->
// check des breed avant le premier "lance un sort" ?
// ne pas prendre en compte les breed après "Instanciation d'une nouvelle invocation" ?
// How to distinct same mob or multiple invocation ? ->
// Is it a problem ?
// A l'initialisation -> ajouter un index par name identique ?
// Pour invoc -> si oop ajout de l'invoc à son character

// Ligne dégats avec état -XX PV (ELEMENT) (NOM DE L'ETAT)
// Total damages includes self-damages -> si contient damageRegex && fighterNameRegex = currentFighter
// Friendly fire ? -> check les noms des perso (si distinction faite entre player et mob)
// How to handle poison/async damages-heal ->
// check after "lance le sort" ->  pseudo: ETAT (+XX Niv.)
// attribuer ETAT au currentFighter
// check quand damage-heal regex avec ETAT dans la ligne

// bug on heals -> when sadida invoke tree and tree levelup -> heals : should not enter in calculation

fun main() {
    val inputFile = File("C:\\Users\\Benoi\\AppData\\Roaming\\zaap\\gamesLogs\\wakfu\\logs\\wakfu.log")
    val outputFile = File("src/main/kotlin/damagesLog/wakfuLogs")

    // not working
    val pattern = """\b[A-Z]{4}\s\d{2}:\d{2}:\d{2},\d{3}\s\["""
    val regex = Regex(pattern)

    inputFile.forEachLine { line ->
        if (regex.matches(line)) {
            val hours = line.substring(6, 8).toInt()
            val minutes = line.substring(9, 11).toInt()
            val seconds = line.substring(12, 14).toInt()
            val millis = line.substring(15, 18).toLong()
            val timeColonPattern = "HH:mm:ss SSS"
            val timeColonFormatter = DateTimeFormatter.ofPattern(timeColonPattern)
            val colonTime = LocalTime.of(hours, minutes, seconds).plus(millis, ChronoUnit.MILLIS)
            println(colonTime)
            println(timeColonFormatter.format(colonTime))
        }
        println("bad line")
    }

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
            fightersDamages[currentFighter] = fightersDamages.getOrDefault(currentFighter, 0) + (damages / 2)

        } else if (inCombat && line.contains(healsRegex)) {
            val heals = line.substringAfterLast("+").trim().substringBefore(" PV").toIntOrNull() ?: 0
            fightersHeals[currentFighter] = fightersHeals.getOrDefault(currentFighter, 0) + (heals / 2)

        } else if (inCombat && line.contains(shieldsRegex)) {
            val shields = line.substringAfterLast(":").trim().substringBefore(" Armure").toIntOrNull() ?: 0
            fightersShields[currentFighter] = fightersShields.getOrDefault(currentFighter, 0) + (shields / 2)

        }
    }
}
