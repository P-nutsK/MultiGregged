package com.p_nsk.multigregged.ars

import com.hollingsworth.arsnouveau.common.block.CreativeSourceJar
import com.hollingsworth.arsnouveau.common.block.SourceJar
import gripe._90.arseng.block.entity.IAdvancedSourceTile
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.max
import kotlin.math.min

object ItemSourceJarTransfer {
    const val MAX_SOURCE = 10000

    @OptIn(ExperimentalContracts::class)
    fun isSourceJar(stack: ItemStack?): Boolean {
        contract {
            returns(true) implies (stack != null)
        }
        if (stack != null) {
            val item = stack.item
            if (item is BlockItem) {
                return item.block is SourceJar
            }
        }
        return false
    }

    private fun isCreativeSourceJar(stack: ItemStack?): Boolean {
        return isSourceJar(stack) && (stack.item as BlockItem).block is CreativeSourceJar
    }

    private fun getSource(sourceJar: ItemStack?): Int {
        if (!isSourceJar(sourceJar)) return -1

        if (isCreativeSourceJar(sourceJar)) {
            return MAX_SOURCE
        }

        return if (sourceJar.hasTag()) {
            sourceJar.getOrCreateTag().getCompound("BlockEntityTag").getInt("source")
        } else {
            0
        }
    }

    private fun changeSource(amount: Int, sourceJar: ItemStack?) {
        if (!isSourceJar(sourceJar)) return

        if (isCreativeSourceJar(sourceJar)) {
            return
        }

        val beTag = sourceJar.getOrCreateTag().getCompound("BlockEntityTag")
        beTag.putInt("source", min(MAX_SOURCE, max(getSource(sourceJar) + amount, 0)))
        beTag.putIntArray("items", IntArray(0))

        sourceJar.getOrCreateTag().put("BlockEntityTag", beTag)
    }

    fun extractSource(stack: ItemStack?, max: Int): Int {
        if (!isSourceJar(stack)) return 0
        if (isCreativeSourceJar(stack)) return max

        val current = getSource(stack)
        val extracted = min(current, max)
        changeSource(-extracted, stack)
        return extracted
    }

    fun insertSource(stack: ItemStack?, max: Int): Int {
        if (!isSourceJar(stack)) return 0
        if (isCreativeSourceJar(stack)) return max

        val current = getSource(stack)
        val inserted = min(MAX_SOURCE - current, max)
        changeSource(inserted, stack)
        return inserted
    }

    fun transferStackToHandler(
        fromStack: ItemStack, toHandler: IAdvancedSourceTile
    ): SourceTransferResult {
        val single = ItemHandlerHelper.copyStackWithSize(fromStack, 1)

        val movable = min(
            getSource(single), toHandler.maxSource - toHandler.source
        )
        if (movable <= 0) {
            return SourceTransferResult(single, 0)
        }

        extractSource(single, movable)
        toHandler.addSource(movable)

        return SourceTransferResult(single, movable)
    }

    fun transferHandlerToStack(
        fromHandler: IAdvancedSourceTile, toStack: ItemStack
    ): SourceTransferResult {
        val single = ItemHandlerHelper.copyStackWithSize(toStack, 1)

        val movable = min(
            fromHandler.source, MAX_SOURCE - getSource(single)
        )
        if (movable <= 0) {
            return SourceTransferResult(single, 0)
        }

        insertSource(single, movable)
        fromHandler.removeSource(movable)

        return SourceTransferResult(single, movable)
    }


    data class SourceTransferResult(
        val stack: ItemStack, val moved: Int
    )


}