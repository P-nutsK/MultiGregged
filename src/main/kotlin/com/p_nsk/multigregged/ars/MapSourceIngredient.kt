package com.p_nsk.multigregged.ars


import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient

@Suppress("EqualsOrHashCode")
class MapSourceIngredient(
    val ingredient: SourceIngredient
) : AbstractMapIngredient() {

    override fun hash(): Int = javaClass.hashCode()
    // ルックアップ時にsourceの完全一致テストをするのは望みではないため、equalsは型のみで判定する
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

