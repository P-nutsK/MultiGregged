package com.p_nsk.multigregged

import com.gregtechceu.gtceu.api.addon.GTAddon
import com.gregtechceu.gtceu.api.addon.IGTAddon
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import com.p_nsk.multigregged.bonk.BonkIngredient
import com.p_nsk.multigregged.bonk.MapBonkIngredient
import net.minecraft.data.recipes.FinishedRecipe
import java.util.function.Consumer

@Suppress("unused")
@GTAddon
class MGDGTAddon : IGTAddon {
    override fun getRegistrate(): GTRegistrate {
        return MultiGreggedMod.REGISTRATE
    }

    override fun initializeAddon() {
        MapIngredientTypeManager.registerMapIngredient(
            BonkIngredient::class.java,
            MapBonkIngredient::convertToMapIngredient
        )
    }

    override fun addonModId(): String {
        return MultiGreggedMod.MOD_ID
    }

    override fun registerTagPrefixes() {
        // CustomTagPrefixes.init();
    }

    override fun addRecipes(provider: Consumer<FinishedRecipe?>?) {
        // CustomRecipes.init(provider);
    }

    override fun registerElements() {
        // CustomElements.init();
    }

    override fun registerRecipeCapabilities() {
        MGDRecipeCapabilities.init()
    }


    // If you have custom bonk types, uncomment this & change to match your capability.
    // KubeJS WILL REMOVE YOUR RECIPES IF THESE ARE NOT REGISTERED.
    /*
     * public static final ContentJS<Double> PRESSURE_IN = new ContentJS<>(NumberComponent.ANY_DOUBLE,
     * CustomRecipeCapabilities.PRESSURE, false);
     * public static final ContentJS<Double> PRESSURE_OUT = new ContentJS<>(NumberComponent.ANY_DOUBLE,
     * CustomRecipeCapabilities.PRESSURE, true);
     *
     * @Override
     * public void registerRecipeKeys(KJSRecipeKeyEvent event) {
     * event.registerKey(CustomRecipeCapabilities.PRESSURE, Pair.of(PRESSURE_IN, PRESSURE_OUT));
     * }
     */

    override fun registerRecipeKeys(event: KJSRecipeKeyEvent) {
        MGDRecipeComponent.registerRecipeKeys(event)

    }
}