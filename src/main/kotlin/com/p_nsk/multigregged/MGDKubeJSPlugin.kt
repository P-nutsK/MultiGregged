package com.p_nsk.multigregged

import dev.latvian.mods.kubejs.KubeJSPlugin
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistryEvent

class MGDKubeJSPlugin : KubeJSPlugin() {
    override fun registerRecipeComponents(event: RecipeComponentFactoryRegistryEvent) {
        MGDRecipeComponent.registerRecipeComponents(event)
    }
}