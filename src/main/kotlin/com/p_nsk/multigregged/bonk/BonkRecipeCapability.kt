@file:Suppress("RedundantNullableReturnType")

package com.p_nsk.multigregged.bonk

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.network.chat.Component
import org.apache.commons.lang3.mutable.MutableInt

class BonkRecipeCapability :
    RecipeCapability<BonkIngredient>("bonk", 0x777777, false, 5, BonkIngredient.Companion.Serializer) {
    companion object {
        @JvmField
        val CAP = BonkRecipeCapability()
    }

    override fun copyInner(content: BonkIngredient): BonkIngredient {
        return content.copy()
    }

    override fun getDefaultMapIngredient(ingredient: Any?): List<AbstractMapIngredient>? {
        val ingredients = ObjectArrayList<AbstractMapIngredient>(1)
        if (ingredient is BonkIngredient) {
            ingredients.add(MapBonkIngredient(ingredient))
        }
        return ingredients
    }

    override fun compressIngredients(ingredients: Collection<Any?>): List<Any?>? {
        var bonkTotal = 0
        for (ingredient in ingredients) {
            if (ingredient is BonkIngredient) {
                bonkTotal += ingredient.bonk
            }
        }
        if (bonkTotal > 0) {
            return ObjectArrayList(listOf(BonkIngredient(bonkTotal)))
        }
        return emptyList()
    }

    override fun addXEIInfo(
        group: WidgetGroup,
        xOffset: Int,
        recipe: GTRecipe,
        contents: List<Content>,
        perTick: Boolean,
        isInput: Boolean,
        yOffset: MutableInt
    ) {
        val bonk = contents.sumOf { CAP.of(it.content).bonk }

        if (isInput) {
            group.addWidget(
                LabelWidget(
                    3 - xOffset,
                    yOffset.addAndGet(10),
                    Component.translatable("xei.multigregged.bonk_input", bonk)
                )
            )
        }
    }

}