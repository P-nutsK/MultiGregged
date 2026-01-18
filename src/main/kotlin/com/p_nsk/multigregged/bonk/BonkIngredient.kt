package com.p_nsk.multigregged.bonk

import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder


data class BonkIngredient(val bonk: Int) {
    companion object {
        @JvmField
        val EMPTY = BonkIngredient(0);

        @JvmField
        val CODEC: Codec<BonkIngredient> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("bonk").forGetter(BonkIngredient::bonk)
            ).apply(instance, ::BonkIngredient)
        }

        object Serializer : IContentSerializer<BonkIngredient> {
            // called by kubejsとか
            override fun of(o: Any?): BonkIngredient? = when (o) {
                is Int -> BonkIngredient(o)
                is BonkIngredient -> o
                else -> null
            }

            override fun defaultValue(): BonkIngredient = EMPTY

            override fun contentClass(): Class<BonkIngredient> = BonkIngredient::class.java

            override fun codec(): Codec<BonkIngredient> = CODEC
        }
    }

    fun copy(): BonkIngredient {
        return BonkIngredient(bonk)
    }


}