package com.p_nsk.multigregged.ars

import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer
import com.mojang.serialization.Codec
import org.apache.commons.lang3.math.NumberUtils


data class SourceIngredient(val source: Int) {
    companion object {
        @JvmField
        val EMPTY = SourceIngredient(0)

        @JvmField
        val CODEC: Codec<SourceIngredient> = Codec.INT.xmap(
            ::SourceIngredient, SourceIngredient::source
        )

        object Serializer : IContentSerializer<SourceIngredient> {

            override fun of(o: Any?): SourceIngredient? {
                return when (o) {
                    is Int -> SourceIngredient(o)
                    is Number -> SourceIngredient(o.toInt())
                    is CharSequence -> {
                        val intValue = NumberUtils.toInt(o.toString(), 0)
                        SourceIngredient(intValue)
                    }

                    is SourceIngredient -> o
                    else -> null
                }
            }

            override fun defaultValue() = EMPTY

            override fun contentClass() = SourceIngredient::class.java

            override fun codec() = CODEC
        }
    }

    fun copy(): SourceIngredient {
        return SourceIngredient(source)
    }
    fun copyWithModifier(modifier:ContentModifier): SourceIngredient {
        return SourceIngredient(modifier.apply(source))
    }

}