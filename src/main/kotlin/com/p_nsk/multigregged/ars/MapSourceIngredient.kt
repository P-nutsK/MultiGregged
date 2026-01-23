package com.p_nsk.multigregged.ars


import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient
import com.p_nsk.multigregged.bonk.MapBonkIngredient

@Suppress("EqualsOrHashCode")
class MapSourceIngredient(
    val ingredient: SourceIngredient
) : AbstractMapIngredient() {

    override fun hash(): Int = javaClass.hashCode()
    // ルックアップ時に全て同じ扱いにするため、equalsは型のみで判定する
    override fun equals(other: Any?): Boolean {
        return other is MapSourceIngredient
    }

    override fun toString() = "MapSourceIngredient{source=$ingredient}"

    companion object {
        fun convertToMapIngredient(
            ingredient: SourceIngredient
        ): List<AbstractMapIngredient> = listOf(MapSourceIngredient(ingredient))
    }
}

