package com.p_nsk.multigregged.ars

import com.gregtechceu.gtceu.utils.FormattingUtil
import com.gregtechceu.gtceu.utils.GTUtil
import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget
import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.jei.IngredientIO
import com.mojang.blaze3d.systems.RenderSystem
import gripe._90.arseng.block.entity.IAdvancedSourceTile
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import java.util.function.BiConsumer
import kotlin.math.max

open class SourceContainerWidget @JvmOverloads constructor(
    val sourceContainer: IAdvancedSourceTile, x: Int, y: Int, width: Int = 18, height: Int = 18,
    // プレイヤーがコンテナを右クリックすることで自分のバケツなどに内容を移せるか
    @Configurable(name = "ldlib.gui.editor.name.allowClickFilled") private var allowClickFilled: Boolean,
    // プレイヤーがコンテナを右クリックすることで自分のバケツなどから内容を補給できるか
    @Configurable(name = "ldlib.gui.editor.name.allowClickDrained") private var allowClickDrained: Boolean
) : Widget(x, y, width, height), IRecipeIngredientSlot, IConfigurableWidget {
    companion object {
        val FLUID_SLOT_TEXTURE: ResourceBorderTexture = ResourceBorderTexture(
            "ldlib:textures/gui/fluid_slot.png", 18, 18, 1, 1
        )
        const val UPDATE_AMOUNT = 0
        const val UPDATE_CAPACITY = 1
        const val UPDATE_CARRIED_AMOUNT = 3
        const val ACTION_CLICK = 1
        val sourceSprite by lazy {
            SourceWidgetRenderUtil.getSprite(
                ResourceLocation.parse("ars_nouveau:block/mana_still")
            )
        }
    }

    @Configurable(name = "ldlib.gui.editor.name.showAmount")
    private var showAmount: Boolean = false
    fun setShowAmount(value: Boolean) {
        showAmount = value
    }

    fun setAllowClickFilled(value: Boolean) {
        allowClickFilled = value
    }

    fun setAllowClickDrained(value: Boolean) {
        allowClickDrained = value
    }


    @Configurable(name = "ldlib.gui.editor.name.drawHoverOverlay")
    var drawHoverOverlay: Boolean = true

    @Configurable(name = "ldlib.gui.editor.name.drawHoverTips")
    private var drawHoverTips: Boolean = false
    fun setDrawHoverTips(value: Boolean) {
        drawHoverTips = value
    }

    @Configurable(name = "ldlib.gui.editor.name.fillDirection")
    private var fillDirection: FillDirection = FillDirection.ALWAYS_FULL
    fun setFillDirection(value: FillDirection) {
        fillDirection = value
    }

    private var onAddedTooltips: BiConsumer<SourceContainerWidget, MutableList<Component>>? = null
    fun setOnAddedTooltips(consumer: BiConsumer<SourceContainerWidget, MutableList<Component>>) {
        onAddedTooltips = consumer
    }

    private var ingredientIO: IngredientIO = IngredientIO.RENDER_ONLY
    override fun getIngredientIO(): IngredientIO? = ingredientIO
    fun setIngredientIO(io: IngredientIO) {
        this.ingredientIO = io
    }

    private var xeiChance: Float = 1f
    override fun getXEIChance(): Float = xeiChance
    fun setXEIChance(chance: Float) {
        this.xeiChance = chance
    }

    var lastAmount: Long = 0
        protected set
    var lastCapacity: Long = 0
        protected set

    private var changeListener: Runnable? = null
    fun setChangeListener(listener: Runnable) {
        changeListener = listener
    }

    override fun initTemplate() {
        setBackground(FLUID_SLOT_TEXTURE)
        setFillDirection(FillDirection.DOWN_TO_UP)
    }

    override fun getXEIIngredients(): List<Any?>? = emptyList()
    override fun setClientSideWidget(): SourceContainerWidget {
        super.setClientSideWidget()
        this.lastCapacity = sourceContainer.maxSource.toLong()
        return this
    }

    override fun getTooltipTexts(): List<Component> {
        val tooltips: MutableList<Component> = getAdditionalTooltips(mutableListOf())
        tooltips.addAll(tooltipTexts)
        return tooltips
    }

    fun getAdditionalTooltips(list: MutableList<Component>): MutableList<Component> {
        onAddedTooltips?.accept(this, list)
        return list
    }

    override fun getFullTooltipTexts(): MutableList<Component> {
        val tooltips: MutableList<Component> = arrayListOf()
        if (sourceContainer.source != 0) {
            tooltips.add(Component.translatable("recipe.capability.source.name"))
            if (showAmount) {
                tooltips.add(
                    Component.translatable(
                        "multigregged.source.amount",
                        FormattingUtil.formatNumbers(lastAmount),
                        FormattingUtil.formatNumbers(lastCapacity)
                    )
                )
            }
        } else {
            tooltips.add(Component.translatable("gtceu.fluid.empty"))
            if (showAmount) {
                tooltips.add(
                    Component.translatable(
                        "multigregged.source.amount", 0, FormattingUtil.formatNumbers(lastCapacity)
                    )
                )
            }
        }
        tooltips.addAll(getTooltipTexts())
        return tooltips
    }

    override fun detectAndSendChanges() {
        val amount = sourceContainer.source.toLong()
        val capacity = sourceContainer.maxSource.toLong()
        if (amount != lastAmount) {
            lastAmount = amount
            writeUpdateInfo(UPDATE_AMOUNT) { buf -> buf.writeVarLong(amount) }
        }

        if (capacity != lastCapacity) {
            lastCapacity = capacity
            writeUpdateInfo(UPDATE_CAPACITY) { buf -> buf.writeVarLong(capacity) }
        }
    }

    override fun readUpdateInfo(id: Int, buf: FriendlyByteBuf) {
        when (id) {
            UPDATE_AMOUNT -> {
                lastAmount = buf.readVarLong()
            }

            UPDATE_CAPACITY -> {
                lastCapacity = buf.readVarLong()
            }

            UPDATE_CARRIED_AMOUNT -> {
                val stack = gui.modularUIContainer.carried
                stack.count = buf.readVarInt()
                gui.modularUIContainer.carried = stack
            }

            else -> {
                super.readUpdateInfo(id, buf)
            }
        }
    }

    override fun writeInitialData(buf: FriendlyByteBuf) {
        lastAmount = sourceContainer.source.toLong()
        lastCapacity = sourceContainer.maxSource.toLong()
        buf.writeVarLong(lastAmount)
        buf.writeVarLong(lastCapacity)
    }

    override fun readInitialData(buf: FriendlyByteBuf) {
        lastAmount = buf.readVarLong()
        lastCapacity = buf.readVarLong()
    }

    // Rendering Nightmare Copied from TankWidget
    @OnlyIn(Dist.CLIENT)
    override fun drawInBackground(
        graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float
    ) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks)

        updateClientCacheIfNeeded()
        drawSource(graphics)
        drawAmountText(graphics)
        drawOverlay(graphics, mouseX, mouseY, partialTicks)
        drawHoverOverlayIfNeeded(graphics, mouseX, mouseY)
    }

    @OnlyIn(Dist.CLIENT)
    private fun updateClientCacheIfNeeded() {
        if (!isClientSideWidget) return

        val capacity = sourceContainer.maxSource.toLong()
        if (capacity != lastCapacity) {
            lastCapacity = capacity
        }

        val amount = sourceContainer.source.toLong()
        if (amount != lastAmount) {
            lastAmount = amount
        }
    }

    private data class DrawRect(
        val x: Float, val y: Float, val width: Float, val height: Float, val progress: Float
    )

    @OnlyIn(Dist.CLIENT)
    private fun computeDrawRect(): DrawRect? {
        if (lastAmount <= 0) return null

        val progress = lastAmount.toDouble() / max(max(lastAmount, lastCapacity), 1)
        val drawnU = fillDirection.getDrawnU(progress).toFloat()
        val drawnV = fillDirection.getDrawnV(progress).toFloat()
        val drawnWidth = fillDirection.getDrawnWidth(progress).toFloat()
        val drawnHeight = fillDirection.getDrawnHeight(progress).toFloat()

        val innerWidth = size.width - 2
        val innerHeight = size.height - 2
        val innerX = position.x + 1
        val innerY = position.y + 1

        return DrawRect(
            x = innerX + drawnU * innerWidth,
            y = innerY + drawnV * innerHeight,
            width = innerWidth * drawnWidth,
            height = innerHeight * drawnHeight,
            progress = progress.toFloat()
        )
    }

    @OnlyIn(Dist.CLIENT)
    private fun drawSource(graphics: GuiGraphics) {
        val rect = computeDrawRect() ?: return

        RenderSystem.disableBlend()
        SourceWidgetRenderUtil.drawSourceLikeFluid(
            graphics = graphics,
            sprite = sourceSprite,
            x = rect.x,
            y = rect.y,
            width = rect.width,
            height = rect.height,
        )
        RenderSystem.enableBlend()
    }

    @OnlyIn(Dist.CLIENT)
    private fun drawAmountText(graphics: GuiGraphics) {
        if (!showAmount || lastAmount <= 0) return
        RenderSystem.disableBlend()

        graphics.pose().pushPose()
        graphics.pose().scale(0.5f, 0.5f, 1f)

        val text = TextFormattingUtil.formatLongToCompactString(lastAmount, 3)
        val font = Minecraft.getInstance().font
        graphics.drawString(
            font,
            text,
            ((position.x + size.width / 3f) * 2 - font.width(text) + 21).toInt(),
            ((position.y + size.height / 3f + 6) * 2).toInt(),
            0xFFFFFF,
            true
        )

        graphics.pose().popPose()
        RenderSystem.enableBlend()
    }

    @OnlyIn(Dist.CLIENT)
    private fun drawHoverOverlayIfNeeded(
        graphics: GuiGraphics, mouseX: Int, mouseY: Int
    ) {
        if (!drawHoverOverlay) return
        if (!isMouseOverElement(mouseX.toDouble(), mouseY.toDouble())) return
        if (getHoverElement(mouseX.toDouble(), mouseY.toDouble()) !== this) return

        RenderSystem.colorMask(true, true, true, false)
        DrawerHelper.drawSolidRect(
            graphics, position.x + 1, position.y + 1, size.width - 2, size.height - 2, 0x80FFFFFF.toInt()
        )
        RenderSystem.colorMask(true, true, true, true)
    }


    @OnlyIn(Dist.CLIENT)
    override fun drawInForeground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (drawHoverTips && isMouseOverElement(
                mouseX.toDouble(), mouseY.toDouble()
            ) && getHoverElement(mouseX.toDouble(), mouseY.toDouble()) === this
        ) {
            if (gui != null) {
                gui.modularUIGui.setHoverTooltip(getFullTooltipTexts(), ItemStack.EMPTY, null, null)
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1f)
        } else {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks)
        }
    }

    @OnlyIn(Dist.CLIENT)
    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if ((allowClickDrained || allowClickFilled) && isMouseOverElement(mouseX, mouseY)) {
            if (button == 0) {
                if (ItemSourceJarTransfer.isSourceJar(gui.modularUIContainer.carried)) {
                    val isShiftKeyDown = isShiftDown()
                    writeClientAction(ACTION_CLICK) { writer -> writer.writeBoolean(isShiftKeyDown) }
                    playButtonClickSound()
                    return true
                }
            }

        }
        return false
    }

    override fun handleClientAction(id: Int, buffer: FriendlyByteBuf) {
        super.handleClientAction(id, buffer)
        if (id == ACTION_CLICK) {
            val isShiftKeyDown = buffer.readBoolean()
            val clickResult: Int = tryClickContainer(isShiftKeyDown)
            if (clickResult >= 0) {
                writeUpdateInfo(UPDATE_CARRIED_AMOUNT) { buf -> buf.writeVarInt(clickResult) }
            }
        }
    }

    private fun tryClickContainer(isShiftKeyDown: Boolean): Int {
        val carried = gui.modularUIContainer.carried
        val player = gui.entityPlayer
        val menu = gui.modularUIContainer
        var result: ItemStack? = null
        if (allowClickFilled) {
            result = transferSourceContainerToStack(player, menu, sourceContainer, isShiftKeyDown)
        }

        if (allowClickDrained && result == null) {
            result = transferSourceStackToContainer(player, menu, sourceContainer, isShiftKeyDown)
        }

        if (result != null) {
            if (gui.modularUIContainer.carried.isEmpty) {
                gui.modularUIContainer.carried = result
            } else {
                gui.modularUIContainer.carried = carried
                giveBack(player, result)
            }
            return gui.modularUIContainer.carried.count
        }
        return -1
    }


    private fun transferSourceStackToContainer(
        player: Player, menu: AbstractContainerMenu, sourceHandler: IAdvancedSourceTile, shift: Boolean
    ): ItemStack? {
        val carried = menu.carried
        if (carried.isEmpty) return null

        val maxAttempts = if (shift) carried.count else 1
        var performedFill = false
        var filledResult = ItemStack.EMPTY
        repeat(maxAttempts) {
            if (carried.isEmpty) return@repeat

            val result = ItemSourceJarTransfer.transferStackToHandler(carried, sourceHandler)
            val remainingStack = result.stack
            if (result.moved <= 0) return@repeat  // ← ここで自然に止まる

            performedFill = true
            carried.shrink(1)
            // 最初ならfilledResultにセット、同じアイテムならスタックを増やす、違うアイテムなら今までのを返して新しいのをセット
            if (filledResult.isEmpty) {
                filledResult = remainingStack.copy()
            } else if (GTUtil.isSameItemSameTags(filledResult, remainingStack)) {
                if (filledResult.count < filledResult.maxStackSize) {
                    filledResult.grow(1)
                } else {
                    giveBack(player, remainingStack)
                }
            } else {
                giveBack(player, filledResult)
                filledResult = remainingStack.copy()
            }
        }
        if (performedFill) {
            player.level().playSound(
                null,
                player.position().x,
                player.position().y + 0.5,
                player.position().z,
                SoundEvents.BUCKET_FILL,
                SoundSource.BLOCKS,
                1.0f,
                1.0f
            )

            return filledResult
        }
        return null
    }

    private fun transferSourceContainerToStack(
        player: Player,
        menu: AbstractContainerMenu,
        sourceHandler: IAdvancedSourceTile,
        shift: Boolean
    ): ItemStack? {
        val carried = menu.carried
        if (carried.isEmpty) return null

        val maxAttempts = if (shift) carried.count else 1
        var performedDrain = false
        var drainedResult = ItemStack.EMPTY

        repeat(maxAttempts) {
            if (carried.isEmpty) return@repeat

            val result = ItemSourceJarTransfer.transferHandlerToStack(sourceHandler, carried)
            val remainingStack = result.stack
            if (result.moved <= 0) return@repeat  // handler が空になったら自然停止

            performedDrain = true
            carried.shrink(1)
            // filled 側と完全対称の集約ロジック
            if (drainedResult.isEmpty) {
                drainedResult = remainingStack.copy()
            } else if (GTUtil.isSameItemSameTags(drainedResult, remainingStack)) {
                if (drainedResult.count < drainedResult.maxStackSize) {
                    drainedResult.grow(1)
                } else {
                    giveBack(player, remainingStack)
                }
            } else {
                giveBack(player, drainedResult)
                drainedResult = remainingStack.copy()
            }
        }

        if (performedDrain) {
            player.level().playSound(
                null,
                player.position().x,
                player.position().y + 0.5,
                player.position().z,
                SoundEvents.BUCKET_EMPTY,
                SoundSource.BLOCKS,
                1.0f,
                1.0f
            )
            return drainedResult
        }

        return null
    }


    private fun giveBack(player: Player, stack: ItemStack) {
        player.inventory.placeItemBackInInventory(stack)
    }


}