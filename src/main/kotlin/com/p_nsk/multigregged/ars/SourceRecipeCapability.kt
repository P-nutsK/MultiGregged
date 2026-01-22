package com.p_nsk.multigregged.ars

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.api.recipe.content.SerializerInteger
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.utils.LocalizationUtils
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.apache.commons.lang3.mutable.MutableInt
import com.p_nsk.multigregged.MultiGreggedMod.Companion.LOGGER


class SourceRecipeCapability : RecipeCapability<Int>("source", 0xC85CCF, false, 5, SerializerInteger.INSTANCE) {
    companion object {
        @JvmField
        val CAP = SourceRecipeCapability()
    }

    override fun copyInner(content: Int): Int {
        return content
    }

    override fun copyWithModifier(content: Int, modifier: ContentModifier): Int {
        return modifier.apply(content)
    }
    // こっちでMapSourceIngredientになってくれそう
    override fun getDefaultMapIngredient(ingredient: Any?): List<AbstractMapIngredient>? {
        LOGGER.info("Getting default map ingredient for ingredient: $ingredient")
        if (ingredient !is Int) return null
        // なぜObjectArrayListを使うのかはわからないけど慣習には従っておけの精神
        val ingredients = ObjectArrayList<AbstractMapIngredient>(1)
        ingredients.add(MapSourceIngredient(ingredient))
        return ingredients
    }

    override fun compressIngredients(ingredients: Collection<Any?>): List<Any?> {
        val sourceTotal = ingredients.filterIsInstance<Int>().sum()
        if (sourceTotal <= 0) {
            return emptyList()
        }
        return ObjectArrayList(listOf(sourceTotal))
    }

    override fun isRecipeSearchFilter(): Boolean = true

    override fun addXEIInfo(
        group: WidgetGroup,
        xOffset: Int,
        recipe: GTRecipe,
        contents: List<Content>,
        perTick: Boolean,
        isInput: Boolean,
        yOffset: MutableInt
    ) {
        val source = contents.sumOf { CAP.of(it.content) }
        val io = if (isInput) "in" else "out"
        val tick = if (perTick) ".tick" else ""
        val x = 3 - xOffset
        val y = yOffset.addAndGet(13)
        group.addWidget(
            LabelWidget(
                x, y, LocalizationUtils.format("multigregged.recipe.source.$io$tick", source)
            )
        )
    }



}