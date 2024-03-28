package oopversion.domain.driven

import oopversion.domain.createfights.models.Fight
import oopversion.domain.models.Fights

interface AllFights {
    fun save(fight: Fight)

    fun fetchAll(): Fights
}