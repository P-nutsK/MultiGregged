package com.p_nsk.multigregged

import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS
import com.mojang.datafixers.util.Pair
import dev.latvian.mods.kubejs.recipe.component.NumberComponent
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistryEvent

object MGDRecipeComponent {

    val BONK_IN = ContentJS(
        NumberComponent.ANY_INT,
        MGDRecipeCapabilities.BONK,
        false
    )
    val BONK_OUT = ContentJS(
        NumberComponent.ANY_INT,
        MGDRecipeCapabilities.BONK,
        true
    )

    fun registerRecipeKeys(event: KJSRecipeKeyEvent) {
        event.registerKey(MGDRecipeCapabilities.BONK, Pair.of(BONK_IN, BONK_OUT))
    }
    fun registerRecipeComponents(event: RecipeComponentFactoryRegistryEvent) {
        event.register("mgdBonkIn", BONK_IN)
        event.register("mgdBonkOut", BONK_OUT)
    }
}