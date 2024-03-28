package oopversion.domain.analyzefights.models

data class AnalyzedFight(
    val id: Long,
    val completeFighters: MutableSet<CompleteFighter>
)