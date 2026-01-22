package com.p_nsk.multigregged.bonk

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient

@Suppress("EqualsOrHashCode")
class MapBonkIngredient(
    val ingredient: BonkIngredient
) : AbstractMapIngredient() {

    override fun hash(): Int =
        ingredient.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MapBonkIngredient) return false
        return ingredient == other.ingredient
    }

    override fun toString(): String =
        "MapBonkIngredient{bonk=$ingredient}"

    companion object {
        fun convertToMapIngredient(
            ingredient: BonkIngredient
        ): List<AbstractMapIngredient> =
            listOf(MapBonkIngredient(ingredient))
    }
}
