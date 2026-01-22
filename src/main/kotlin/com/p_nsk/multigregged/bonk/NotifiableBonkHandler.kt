package com.p_nsk.multigregged.bonk

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.trait.ICapabilityTrait
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait
import com.gregtechceu.gtceu.api.recipe.GTRecipe

class NotifiableBonkHandler @JvmOverloads constructor(
    machine: MetaMachine,
    val bonkCapacity: Int,
    private val handlerIO: IO,
    private val capabilityIO: IO = handlerIO,
) : NotifiableRecipeHandlerTrait<BonkIngredient>(machine),
    ICapabilityTrait {
    override fun getHandlerIO(): IO = handlerIO
    override fun getCapabilityIO(): IO = capabilityIO
    var bonk = 0
    fun addBonk(bonkToAdd: Int, simulate: Boolean): Boolean {
        if (bonkToAdd < 0) return false
        if (bonkToAdd.toLong() + this.bonk.toLong() > this.bonkCapacity) return false
        if (simulate) return true
        bonk += bonkToAdd
        this.notifyListeners()
        return true
    }

    fun drainBonk(bonkToDrain: Int, simulate: Boolean): Boolean {
        if (bonkToDrain < 0) return false
        if (bonkToDrain > this.bonk) return false
        if (simulate) return true
        bonk -= bonkToDrain
        this.notifyListeners()
        return true
    }

    override fun handleRecipeInner(
        io: IO,
        recipe: GTRecipe,
        left: MutableList<BonkIngredient>,
        simulate: Boolean
    ): List<BonkIngredient>? {
        for (i in left.indices) {
            val bonkIngredient = left[i]
            if (bonk >= bonkIngredient.bonk) {
                if (!simulate) {
                    bonk -= bonkIngredient.bonk
                }
                left.removeAt(i)
                break
            }
        }
        return if (left.isEmpty()) null else left
    }

    override fun getContents(): List<Any?> {
        return listOf(BonkIngredient(bonk))
    }

    override fun getTotalContentAmount(): Double {
        return 1.0;
    }

    override fun getCapability(): RecipeCapability<BonkIngredient> {
        return BonkRecipeCapability.CAP
    }
}