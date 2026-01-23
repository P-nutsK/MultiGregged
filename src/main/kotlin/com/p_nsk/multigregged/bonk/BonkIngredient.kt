package com.p_nsk.multigregged.bonk

import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer
import com.mojang.serialization.Codec
import org.apache.commons.lang3.math.NumberUtils


data class BonkIngredient(val bonk: Int) {
    companion object {
        @JvmField
        val EMPTY = BonkIngredient(0)

        @JvmField
        val CODEC: Codec<BonkIngredient> = Codec.INT.xmap(
            ::BonkIngredient, BonkIngredient::bonk
        )

        object Serializer : IContentSerializer<BonkIngredient> {

            override fun of(o: Any?): BonkIngredient? {
                return when (o) {
                    is Int -> BonkIngredient(o)
                    is Number -> BonkIngredient(o.toInt())
                    is CharSequence -> {
                        val intValue = NumberUtils.toInt(o.toString(), 0)
                        BonkIngredient(intValue)
                    }

                    is BonkIngredient -> o
                    else -> null
                }
            }

            override fun defaultValue() = EMPTY

            override fun contentClass() = BonkIngredient::class.java

            override fun codec() = CODEC
        }
    }

    fun copy(): BonkIngredient {
        return BonkIngredient(bonk)
    }


}