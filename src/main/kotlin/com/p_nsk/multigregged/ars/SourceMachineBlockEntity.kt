package com.p_nsk.multigregged.ars

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity
import com.hollingsworth.arsnouveau.api.client.ITooltipProvider
import gripe._90.arseng.block.entity.IAdvancedSourceTile
import gripe._90.arseng.definition.ArsEngCapabilities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import com.p_nsk.multigregged.MultiGreggedMod.Companion.LOGGER

class SourceMachineBlockEntity(type: BlockEntityType<*>, pos: BlockPos, blockState: BlockState) :
    MetaMachineBlockEntity(type, pos, blockState), ITooltipProvider {
    private val sourceCap: LazyOptional<IAdvancedSourceTile> = LazyOptional.of {
        (metaMachine as ISourceMachine).getSourceHandler()
    }

    override fun getTooltip(tooltip: MutableList<Component>) {
        val machine = metaMachine as? ISourceMachine ?: return
        val handler = machine.getSourceHandler()

        val max = handler.maxSource
        // Avoid division by zero
        if (max <= 0) return
        val percent = (handler.source.toDouble() / max * 100).toInt()

        tooltip.add(
            Component.translatable(
                "ars_nouveau.source_jar.fullness",
                percent
            )
        )
    }

    override fun <T> getCapability(
        cap: Capability<T>, side: Direction?
    ): LazyOptional<T> {
        if (cap == ArsEngCapabilities.SOURCE_TILE) {
            if (metaMachine is ISourceMachine) {
                return sourceCap.cast()
            }
        }

        return super.getCapability(cap, side)
    }

    override fun invalidateCaps() {
        LOGGER.debug("[SourceBE] invalidateCaps pos={} metaMachine={}", blockPos, metaMachine?.javaClass?.name)
        super.invalidateCaps()
        sourceCap.invalidate()
    }

}