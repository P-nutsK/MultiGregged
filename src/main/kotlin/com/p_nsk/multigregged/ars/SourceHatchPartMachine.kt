package com.p_nsk.multigregged.ars

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour
import com.gregtechceu.gtceu.config.ConfigHolder
import com.hollingsworth.arsnouveau.client.particle.ParticleUtil
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.syncdata.ISubscription
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import com.p_nsk.multigregged.base.AbstractTieredAutoIOPartMachine
import com.p_nsk.multigregged.util.orNull
import gripe._90.arseng.block.entity.IAdvancedSourceTile
import gripe._90.arseng.definition.ArsEngCapabilities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import kotlin.math.min

open class SourceHatchPartMachine(holder: IMachineBlockEntity, tier: Int, io: IO, initialCapacity: Int) :
    AbstractTieredAutoIOPartMachine<SourceHatchPartMachine>(holder, tier, io), IHasCircuitSlot, IMachineLife,
    IInteractedMachine, ISourceMachine {

    companion object {
        fun getMaxCapacity(initialCapacity: Int, tier: Int): Int {
            return initialCapacity * (1 shl min(9, tier))
        }

        fun getMaxConsumption(initialCapacity: Int, tier: Int): Int {
            return getMaxCapacity(initialCapacity, tier)
        }

        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(
            SourceHatchPartMachine::class.java, TieredIOPartMachine.MANAGED_FIELD_HOLDER
        )
    }

    override fun getFieldHolder(): ManagedFieldHolder = MANAGED_FIELD_HOLDER

    @Persisted
    @DescSynced
    val sourceContainer = NotifiableSourceContainer(
        this, getMaxCapacity(initialCapacity, tier), getMaxConsumption(initialCapacity, tier), io
    )

    // SourceContainerの下に置く必要がある handlerIOの決定の問題
    private val circuitInventory: NotifiableItemStackHandler = createCircuitItemHandler().shouldSearchContent(false)
    private var circuitSlotEnabled = true

    override fun getSourceHandler(): IAdvancedSourceTile = sourceContainer

    ////---------------------------------- Circuit Things ----------------------------------//
    override fun getCircuitInventory(): NotifiableItemStackHandler = circuitInventory

    // copied from FluidHatchPartMachine
    protected fun createCircuitItemHandler(): NotifiableItemStackHandler {
        return if (io == IO.IN) {
            NotifiableItemStackHandler(
                this, 1, IO.IN, IO.NONE
            ).setFilter { itemStack: ItemStack? -> IntCircuitBehaviour.isIntegratedCircuit(itemStack) }
        } else {
            NotifiableItemStackHandler(this, 0, IO.NONE)
        }
    }

    override fun isCircuitSlotEnabled() = circuitSlotEnabled

    override fun attachConfigurators(configuratorPanel: ConfiguratorPanel) {
        super.attachConfigurators(configuratorPanel)
        if (isCircuitSlotEnabled && this.io == IO.IN) {
            configuratorPanel.attachConfigurators(CircuitFancyConfigurator(circuitInventory.storage))
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
            .addWidget(LabelWidget(8, 18) { sourceContainer.source.toString() })
        group.addWidget(
            SourceContainerWidget(sourceContainer, 67, 22, 18, 18, true, io.support(IO.IN))
                .apply {
                    setShowAmount(true)
                    setDrawHoverTips(true)
                    setBackground(GuiTextures.FLUID_SLOT)
                })

        group.setBackground(GuiTextures.BACKGROUND_INVERSE)
        return group
    }

    // TODO: 色をつけられるようにするやつ。いらない気もするけど今は中途半端だから
    ///---------------------------------- Auto IO Things ----------------------------------//
    override fun hasAutoOutputContent(): Boolean = !sourceContainer.isEmpty()
    override fun hasIOTarget(facing: Direction): Boolean {
        val pos = holder.pos().relative(facing)
        val be = holder.level().getBlockEntity(pos) ?: return false

        return be.getCapability(ArsEngCapabilities.SOURCE_TILE, facing.opposite).isPresent
    }

    override fun autoInput(facing: Direction) {
        val adjacent = getAdjacentSourceProvider(facing) ?: return
        if (!adjacent.container.relayCanTakePower()) return

        transferSource(
            from = adjacent, to = SourceProvider(holder.pos(), sourceContainer)
        )
    }

    override fun autoOutput(facing: Direction) {
        val adjacent = getAdjacentSourceProvider(facing) ?: return
        if (!adjacent.container.canAcceptSource()) return

        transferSource(
            from = SourceProvider(holder.pos(), sourceContainer), to = adjacent
        )
    }

    // ArsのAPIはextracted/insertedを返す代わりにcurrentSourceを返すので注意
    private fun transferSource(from: SourceProvider, to: SourceProvider) {
        val transfer = negotiateTransfer(
            senderSource = from.container.source,
            senderRate = from.container.transferRate,
            receiverSource = to.container.source,
            receiverMax = to.container.maxSource
        )
        if (transfer <= 0) return

        to.container.addSource(transfer)
        from.container.removeSource(transfer)
        ParticleUtil.spawnFollowProjectile(level, from.pos, to.pos)
    }

    private fun negotiateTransfer(
        senderSource: Int, senderRate: Int, receiverSource: Int, receiverMax: Int
    ): Int {
        val canSend = min(senderRate, senderSource)
        if (canSend <= 0) return 0

        val canReceive = receiverMax - receiverSource
        if (canReceive <= 0) return 0

        return min(canSend, canReceive)
    }


    private fun getAdjacentSourceProvider(facing: Direction): SourceProvider? {
        val pos = holder.pos().relative(facing)
        val be = holder.level().getBlockEntity(pos) ?: return null

        val container = be.getCapability(ArsEngCapabilities.SOURCE_TILE, facing.opposite).orNull() ?: return null

        return SourceProvider(pos, container)
    }

    // SpecialSourceProviderから命名
    data class SourceProvider(val pos: BlockPos, val container: IAdvancedSourceTile)


    override fun subscribeContainerChanges(callback: Runnable): ISubscription {
        return sourceContainer.addChangedListener(callback)
    }

    // ---------------------------------- SwapIO Things ----------------------------------//
    override fun getSwappedIODefinition(): MachineDefinition? {
        return when (io) {
            IO.IN -> ArsMachines.SOURCE_OUTPUT_HATCH[this.tier]
            IO.OUT -> ArsMachines.SOURCE_INPUT_HATCH[this.tier]
            else -> null
        }
    }

    override fun cloneStateToNewMachine(newMachine: SourceHatchPartMachine) {
        newMachine.sourceContainer.source = this.sourceContainer.source
    }


}
