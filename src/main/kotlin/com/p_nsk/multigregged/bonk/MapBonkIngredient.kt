package com.p_nsk.multigregged.bonk

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient

@Suppress("EqualsOrHashCode")
class MapBonkIngredient(
    val ingredient: BonkIngredient
) : AbstractMapIngredient() {

    override fun hash(): Int = javaClass.hashCode()
    // ルックアップ時に全て同じ扱いにするため、equalsは型のみで判定する
    override fun equals(other: Any?): Boolean {
        return other is MapBonkIngredient
    }

    override fun toString(): String = "MapBonkIngredient{bonk=$ingredient}"

    companion object {
        fun convertToMapIngredient(
            ingredient: BonkIngredient
        ): List<AbstractMapIngredient> = listOf(MapBonkIngredient(ingredient))
    }
}
