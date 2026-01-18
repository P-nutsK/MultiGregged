package com.p_nsk.multigregged.bonk

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.BlockHitResult
import kotlin.math.min

class BonkHatchPartMachine(holder: IMachineBlockEntity, tier: Int, io: IO,initialCapacity: Int) : TieredIOPartMachine(holder, tier, io) {
    companion object {
        fun getBonkCapacity(initialCapacity: Int, tier: Int): Int {
            return initialCapacity * (1 shl min(9, tier))
        }
    }

    val bonkHandler = NotifiableBonkHandler(this, getBonkCapacity(initialCapacity,tier), io)

    override fun onHardHammerClick(
        playerIn: Player,
        hand: InteractionHand,
        gridSide: Direction,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (isRemote) return InteractionResult.SUCCESS;
        if (bonkHandler.addBonk(1, false)) {
            playerIn.sendSystemMessage(Component.literal("Bonk! Total bonk stored: " + bonkHandler.bonk));
            return InteractionResult.CONSUME;
        }
        return super.onHardHammerClick(playerIn, hand, gridSide, hitResult);
    }

}