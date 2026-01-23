package com.p_nsk.multigregged

import com.gregtechceu.gtceu.api.registry.GTRegistries
import com.p_nsk.multigregged.ars.SourceRecipeCapability
import com.p_nsk.multigregged.bonk.BonkRecipeCapability

object MGDRecipeCapabilities {
    @JvmField
    val BONK = BonkRecipeCapability.CAP
    @JvmField
    val SOURCE = SourceRecipeCapability.CAP
    fun init() {
        GTRegistries.RECIPE_CAPABILITIES.register(BONK.name, BONK)
        GTRegistries.RECIPE_CAPABILITIES.register(SOURCE.name, SOURCE)
    }
}