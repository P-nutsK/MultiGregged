package com.p_nsk.multigregged

import com.gregtechceu.gtceu.api.registry.GTRegistries
import com.p_nsk.multigregged.bonk.BonkRecipeCapability

object MGDRecipeCapabilities {
    @JvmStatic
    val BONK = BonkRecipeCapability.CAP
    fun init() {
        GTRegistries.RECIPE_CAPABILITIES.register(BONK.name, BONK)
    }
}