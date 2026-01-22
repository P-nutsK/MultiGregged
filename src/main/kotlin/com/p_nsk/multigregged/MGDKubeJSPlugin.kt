package com.p_nsk.multigregged

import com.gregtechceu.gtceu.api.capability.recipe.IO
import dev.latvian.mods.kubejs.KubeJSPlugin
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistryEvent
import dev.latvian.mods.kubejs.script.BindingsEvent
import dev.latvian.mods.kubejs.script.ScriptType
import dev.latvian.mods.kubejs.util.ClassFilter

class MGDKubeJSPlugin : KubeJSPlugin() {
    override fun registerRecipeComponents(event: RecipeComponentFactoryRegistryEvent) {
        MGDRecipeComponent.registerRecipeComponents(event)
    }

    override fun registerClasses(type: ScriptType, filter: ClassFilter) {
        super.registerClasses(type, filter)
        filter.allow("com.p_nsk.multigregged");
    }

    override fun registerBindings(event: BindingsEvent) {
        event.add("MGDPartAbilities", MGDPartAbilities)
        event.add("MGDRecipeCapabilities", MGDRecipeCapabilities)
        // todo arsとか
        event.add("IO", IO::class.java)

    }
    // todo event.create("foo","source_machine")できるようにする
}