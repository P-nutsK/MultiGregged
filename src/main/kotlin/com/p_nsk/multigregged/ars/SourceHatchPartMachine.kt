package com.p_nsk.multigregged.ars

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour
import com.gregtechceu.gtceu.config.ConfigHolder
import com.hollingsworth.arsnouveau.api.source.ISourceTile
import com.hollingsworth.arsnouveau.api.source.ISpecialSourceProvider
import com.hollingsworth.arsnouveau.api.source.SourceManager
import com.hollingsworth.arsnouveau.common.items.DominionWand
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import gripe._90.arseng.block.entity.IAdvancedSourceTile
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import java.util.function.Supplier
import kotlin.math.min
import com.p_nsk.multigregged.MultiGreggedMod.Companion.LOGGER

@Suppress("PROPERTY_HIDES_JAVA_FIELD")
open class SourceHatchPartMachine(holder: IMachineBlockEntity, tier: Int, io: IO, initialCapacity: Int) :
    TieredIOPartMachine(holder, tier, io),
     IHasCircuitSlot, IMachineLife, IInteractedMachine, ISourceMachine {

    private val circuitInventory: NotifiableItemStackHandler = createCircuitItemHandler().shouldSearchContent(false)
    private var circuitSlotEnabled = true

    companion object {
        fun getMaxCapacity(initialCapacity: Int, tier: Int): Int {
            return initialCapacity * (1 shl min(9, tier))
        }

        fun getMaxConsumption(initialCapacity: Int, tier: Int): Int {
            return getMaxCapacity(initialCapacity, tier)
        }

        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(
            SourceHatchPartMachine::class.java,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER
        )
    }

    override fun getFieldHolder(): ManagedFieldHolder = MANAGED_FIELD_HOLDER

    @field:Persisted
    @field:DescSynced
    val sourceContainer =
        NotifiableSourceContainer(
            this,
            getMaxCapacity(initialCapacity, tier),
            getMaxConsumption(initialCapacity, tier),
            io
        )

    ////---------------------------------- Circuit Things ----------------------------------//
    override fun getCircuitInventory(): NotifiableItemStackHandler = circuitInventory

    // copied from FluidHatchPartMachine
    protected fun createCircuitItemHandler(): NotifiableItemStackHandler {
        return if (io == IO.IN) {
            NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE)
                .setFilter { itemStack: ItemStack? -> IntCircuitBehaviour.isIntegratedCircuit(itemStack) }
        } else {
            NotifiableItemStackHandler(this, 0, IO.NONE)
        }
    }

    override fun isCircuitSlotEnabled() = circuitSlotEnabled

    override fun attachConfigurators(configuratorPanel: ConfiguratorPanel) {
        super.attachConfigurators(configuratorPanel);
        if (isCircuitSlotEnabled && this.io == IO.IN) {
            configuratorPanel.attachConfigurators(CircuitFancyConfigurator(circuitInventory.storage));
        }
    }

    override fun onMachineRemoved() {
        if (!ConfigHolder.INSTANCE.machines.ghostCircuit) {
            clearInventory(circuitInventory.storage)
        }
    }

    override fun addedToController(controller: IMultiController) {
        if (!controller.allowCircuitSlots()) {
            if (!ConfigHolder.INSTANCE.machines.ghostCircuit) {
                clearInventory(circuitInventory.storage)
            } else {
                circuitInventory.setStackInSlot(0, ItemStack.EMPTY)
            }
            circuitSlotEnabled = false
        }
        super.addedToController(controller)
    }

    override fun removedFromController(controller: IMultiController) {
        super.removedFromController(controller)
        for (c in controllers) {
            if (!c.allowCircuitSlots()) {
                return
            }
        }
        circuitSlotEnabled = true
    }
    //---------------------------------- UI Things ----------------------------------//

    override fun createUIWidget(): Widget? {
        val group = WidgetGroup(0, 0, 89, 63)
        group.addWidget(ImageWidget(4, 4, 81, 55, GuiTextures.DISPLAY))

        group.addWidget(LabelWidget(8, 8, "multigregged.gui.source"))
            .addWidget(LabelWidget(8, 18, { sourceContainer.source.toString() }))

        if(isRemote) {
            LOGGER.info("sourceContainer source: ${sourceContainer.source}")
        }

        group.setBackground(GuiTextures.BACKGROUND_INVERSE)
        return group
    }
    // Dominion WandがインタラクションしたときはGUIを開かないようにSUCCESSを返す
    // GUIを無くすという選択肢もある。要検討
    override fun onUse(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {

        val heldItem = player.getItemInHand(hand)
        if (heldItem.item is DominionWand) {
            return InteractionResult.SUCCESS
        }

        return InteractionResult.PASS
    }

    override fun getSourceHandler(): IAdvancedSourceTile = sourceContainer

}