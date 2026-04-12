package com.p_nsk.multigregged

import com.p_nsk.multigregged.bonk.BonkIngredient
import dev.latvian.mods.rhino.util.wrap.TypeWrappers
import net.minecraft.world.item.ItemStack

object MGDCast {
    @JvmStatic
    fun bonk(bonk: BonkIngredient): BonkIngredient = bonk
    @JvmStatic
    fun item(item: ItemStack) : ItemStack = item
}
