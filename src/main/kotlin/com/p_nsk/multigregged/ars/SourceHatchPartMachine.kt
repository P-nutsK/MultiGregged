package com.p_nsk.multigregged.ars

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour
import com.gregtechceu.gtceu.config.ConfigHolder
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.syncdata.ISubscription
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import com.p_nsk.multigregged.MultiGreggedMod.Companion.LOGGER
import gripe._90.arseng.block.entity.IAdvancedSourceTile
import gripe._90.arseng.definition.ArsEngCapabilities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.TickTask
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraftforge.common.util.LazyOptional
import kotlin.math.min

@Suppress("PROPERTY_HIDES_JAVA_FIELD")
open class SourceHatchPartMachine(holder: IMachineBlockEntity, tier: Int, io: IO, initialCapacity: Int) :
    TieredIOPartMachine(holder, tier, io),
    IHasCircuitSlot, IMachineLife, IInteractedMachine, ISourceMachine {

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

    @Persisted
    @DescSynced
    val sourceContainer =
        NotifiableSourceContainer(
            this,
            getMaxCapacity(initialCapacity, tier),
            getMaxConsumption(initialCapacity, tier),
            io
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
            NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE)
                .setFilter { itemStack: ItemStack? -> IntCircuitBehaviour.isIntegratedCircuit(itemStack) }
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
        LOGGER.info(
            "[SourceHatch] addedToController tier={} io={} controller={} pos={} capsIn={} capsOut={} source={}/{}",
            tier,
            this.io,
            controller.javaClass.simpleName,
            holder.pos(),
            sourceContainer.canCapInput(),
            sourceContainer.canCapOutput(),
            sourceContainer.source,
            sourceContainer.maxSource
        )
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
        LOGGER.info(
            "[SourceHatch] removedFromController tier={} io={} controller={} pos={} source={}/{}",
            tier,
            this.io,
            controller.javaClass.simpleName,
            holder.pos(),
            sourceContainer.source,
            sourceContainer.maxSource
        )
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

        if (isRemote) {
            LOGGER.info(
                "[SourceHatch] UI tick pos={} tier={} io={} source={}/{} canIn={} canOut={}",
                holder.pos(),
                tier,
                this.io,
                sourceContainer.source,
                sourceContainer.maxSource,
                sourceContainer.canCapInput(),
                sourceContainer.canCapOutput()
            )
        }

        group.setBackground(GuiTextures.BACKGROUND_INVERSE)
        return group
    }

    // TODO: 前のブロックとの自動IOの実装
    // TODO: GUIの改善 スロットを追加してソースジャーとIOできるとか？ ちょっとずるいのでConfig化かも
    // TODO: 色をつけられるようにするやつ。いらない気もするけど今は中途半端だから
    // Modular Machineryみたいに可愛いやつがいい
    ///---------------------------------- Auto IO Things ----------------------------------//
    private var autoIOSub: TickableSubscription? = null
    override fun onNeighborChanged(block: Block, fromPos: BlockPos, isMoving: Boolean) {
        super.onNeighborChanged(block, fromPos, isMoving)
        updateSubscription()
    }

    override fun setWorkingEnabled(workingEnabled: Boolean) {
        super.setWorkingEnabled(workingEnabled)
        updateSubscription()
    }

    // TODO Onrotated

    fun updateSubscription(newFacing: Direction = frontFacing) {
        if (workingEnabled) {
            val canOutput = io.support(IO.OUT) && !sourceContainer.isEmpty()
            val canInput = io.support(IO.IN)
            val canIO = canOutput || canInput
            if (canIO && hasAdjacentSourceContainer()) {
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
                inputFromAdjacent()
            }
            if (io.support(IO.OUT)) {
                outputToAdjacent()
            }
        }
        updateSubscription()
    }

    // ArsのAPIはextracted/insertedを返す代わりにcurrentSourceを返すので注意
    private fun outputToAdjacent() {
        val adjacent = getAdjacentSourceContainer() ?: return
        if (!adjacent.canAcceptSource()) return

        val transfer = negotiateTransfer(
            senderSource = sourceContainer.source,
            senderRate = sourceContainer.transferRate,
            receiverSource = adjacent.source,
            receiverMax = adjacent.maxSource
        )
        if (transfer <= 0) return

        adjacent.addSource(transfer)
        sourceContainer.removeSource(transfer)
    }

    private fun inputFromAdjacent() {
        val adjacent = getAdjacentSourceContainer() ?: return
        if (!adjacent.relayCanTakePower()) return

        val transfer = negotiateTransfer(
            senderSource = adjacent.source,
            senderRate = adjacent.transferRate,
            receiverSource = sourceContainer.source,
            receiverMax = sourceContainer.maxSource
        )
        if (transfer <= 0) return

        sourceContainer.addSource(transfer)
        adjacent.removeSource(transfer)
    }

    private fun negotiateTransfer(
        senderSource: Int,
        senderRate: Int,
        receiverSource: Int,
        receiverMax: Int
    ): Int {
        val canSend = min(senderRate, senderSource)
        if (canSend <= 0) return 0

        val canReceive = receiverMax - receiverSource
        if (canReceive <= 0) return 0

        return min(canSend, canReceive)
    }


    private fun getAdjacentSourceCapability(): LazyOptional<IAdvancedSourceTile> {
        val neighborPos = holder.pos().relative(frontFacing)
        val neighborBE = holder.level().getBlockEntity(neighborPos) ?: return LazyOptional.empty()
        return neighborBE.getCapability(ArsEngCapabilities.SOURCE_TILE, frontFacing.opposite)
    }

    private fun getAdjacentSourceContainer(): IAdvancedSourceTile? {
        return getAdjacentSourceCapability().orElse(null)
    }

    private fun hasAdjacentSourceContainer(): Boolean {
        return getAdjacentSourceCapability().isPresent
    }

    var containerSub: ISubscription? = null
    override fun onLoad() {
        super.onLoad()
        if (level is ServerLevel) {
            (level as ServerLevel).server.tell(TickTask(0, ::updateSubscription))
        }
        containerSub = sourceContainer.addChangedListener(::updateSubscription)
    }

    override fun onUnload() {
        super.onUnload()
        containerSub?.unsubscribe().also { containerSub = null }
    }

}
