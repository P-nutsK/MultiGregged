package com.p_nsk.multigregged.base

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.lowdragmc.lowdraglib.syncdata.ISubscription
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.TickTask
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.BlockHitResult

abstract class AbstractTieredAutoIOPartMachine<SELF : AbstractTieredAutoIOPartMachine<SELF>>(
    holder: IMachineBlockEntity, tier: Int, io: IO
) : TieredIOPartMachine(
    holder, tier, io
), IMachineLife {
    private var autoIOSub: TickableSubscription? = null
    override fun onNeighborChanged(block: Block, fromPos: BlockPos, isMoving: Boolean) {
        super.onNeighborChanged(block, fromPos, isMoving)
        updateIOSubscription()
    }

    override fun setWorkingEnabled(workingEnabled: Boolean) {
        super.setWorkingEnabled(workingEnabled)
        updateIOSubscription()
    }

    override fun onRotated(oldFacing: Direction, newFacing: Direction) {
        super.onRotated(oldFacing, newFacing)
        updateIOSubscription(newFacing)
    }

    abstract fun hasAutoOutputContent(): Boolean
    abstract fun hasIOTarget(facing: Direction): Boolean

    fun updateIOSubscription(newFacing: Direction = frontFacing) {
        if (workingEnabled) {
            val canOutput = io.support(IO.OUT) && hasAutoOutputContent()
            val canInput = io.support(IO.IN)
            val canIO = canOutput || canInput
            if (canIO && hasIOTarget(newFacing)) {
                autoIOSub = subscribeServerTick(autoIOSub, ::autoIO)
            }
        } else {
            autoIOSub?.unsubscribe().also {
                autoIOSub = null
            }
        }
    }

    private fun autoIO() {
        if (offsetTimer % 5 != 0L) return
        if (workingEnabled) {
            if (io.support(IO.IN)) {
                autoInput(frontFacing)
            }
            if (io.support(IO.OUT)) {
                autoOutput(frontFacing)
            }
        }
        updateIOSubscription()
    }

    abstract fun autoInput(facing: Direction)
    abstract fun autoOutput(facing: Direction)
    abstract fun subscribeContainerChanges(callback: Runnable): ISubscription

    var containerSub: ISubscription? = null
    override fun onLoad() {
        super.onLoad()
        if (level is ServerLevel) {
            (level as ServerLevel).server.tell(TickTask(0, ::updateIOSubscription))
        }
        containerSub = subscribeContainerChanges(::updateIOSubscription)
    }

    override fun onUnload() {
        super.onUnload()
        containerSub?.unsubscribe().also { containerSub = null }
    }

    override fun onScrewdriverClick(
        playerIn: Player, hand: InteractionHand, gridSide: Direction, hitResult: BlockHitResult
    ): InteractionResult {
        super.onScrewdriverClick(playerIn, hand, gridSide, hitResult).takeIf { it != InteractionResult.PASS }
            ?.let { return it }

        if (io == IO.BOTH) return InteractionResult.PASS
        return if (playerIn.isShiftKeyDown && swapIO()) InteractionResult.sidedSuccess(playerIn.level().isClientSide) else InteractionResult.PASS

    }

    abstract fun getSwappedIODefinition(): MachineDefinition?
    abstract fun cloneStateToNewMachine(newMachine: SELF)
    @JvmName("cloneStateToNewMachine_internal")
    @Suppress("UNCHECKED_CAST")
    private fun cloneStateToNewMachine(newMachine: AbstractTieredAutoIOPartMachine<*>) {
        cloneStateToNewMachine(newMachine as SELF)
    }

    fun swapIO(): Boolean {
        val level = level ?: return false
        val newDefinition: MachineDefinition = getSwappedIODefinition() ?: return false

        level.setBlockAndUpdate(pos, newDefinition.block.defaultBlockState())
        //#region お目汚し newMachine取得
        val newHolder = level.getBlockEntity(pos) as? IMachineBlockEntity ?: return true // should not happen
        val newMachine = newHolder.metaMachine as? AbstractTieredAutoIOPartMachine<SELF> ?: return true // should not happen
        //#endregion
        val old = this
        newMachine.apply {
            frontFacing = old.frontFacing
            upwardsFacing = old.upwardsFacing
            paintingColor = old.paintingColor
        }
        cloneStateToNewMachine(newMachine)
        return true
    }

}