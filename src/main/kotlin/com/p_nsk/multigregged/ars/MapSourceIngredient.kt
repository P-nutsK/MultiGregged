package com.p_nsk.multigregged.ars


import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient

@Suppress("EqualsOrHashCode")
class MapSourceIngredient(
    val source: Int
) : AbstractMapIngredient() {

    override fun hash(): Int = source.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MapSourceIngredient) return false
        return source == other.source
    }

    override fun toString(): String = "MapSourceIngredient{source=$source}"
    // これあんまやるべきじゃなさそう
    companion object {
        @JvmStatic
        fun convertToMapIngredient(
            source: Int
        ): List<AbstractMapIngredient> = listOf(MapSourceIngredient(source))
    }
}
