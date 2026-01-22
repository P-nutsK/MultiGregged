//package com.p_nsk.multigregged.mekanism.chemical
//
//import com.gregtechceu.gtceu.GTCEu
//import com.gregtechceu.gtceu.api.gui.widget.TankWidget
//import com.gregtechceu.gtceu.integration.xei.handlers.fluid.CycleFluidEntryHandler
//import com.gregtechceu.gtceu.integration.xei.handlers.fluid.CycleFluidStackHandler
//import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable
//import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget
//import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot
//import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture
//import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture
//import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture
//import com.lowdragmc.lowdraglib.gui.widget.Widget
//import com.lowdragmc.lowdraglib.jei.ClickableIngredient
//import com.lowdragmc.lowdraglib.jei.IngredientIO
//import com.lowdragmc.lowdraglib.jei.JEIPlugin
//import com.lowdragmc.lowdraglib.utils.Position
//import com.lowdragmc.lowdraglib.utils.Size
//import mekanism.api.chemical.Chemical
//import mekanism.api.chemical.ChemicalStack
//import mekanism.api.chemical.IChemicalHandler
//import net.minecraft.nbt.CompoundTag
//import net.minecraft.network.chat.Component
//import net.minecraftforge.common.capabilities.Capability
//import java.util.function.BiConsumer
//
//
//abstract class ChemicalTankWidget<CHEMICAL : Chemical<CHEMICAL>, STACK : ChemicalStack<CHEMICAL>> : Widget,
//    IRecipeIngredientSlot, IConfigurableWidget {
//
//    companion object {
//        @JvmField
//        val FLUID_SLOT_TEXTURE = ResourceBorderTexture("ldlib:textures/gui/fluid_slot.png", 18, 18, 1, 1)
//    }
//
//    protected var chemicalHandler: IChemicalHandler<CHEMICAL, STACK>? = null
//
//    protected var tank: Int = 0
//
//    @field:Configurable(name = "ldlib.gui.editor.name.showAmount")
//    var showAmount: Boolean = true
//
//    @field:Configurable(name = "ldlib.gui.editor.name.allowClickFilled")
//    var allowClickFilled: Boolean = true
//
//    @field:Configurable(name = "ldlib.gui.editor.name.allowClickDrained")
//    var allowClickDrained: Boolean = true
//
//    @field:Configurable(name = "ldlib.gui.editor.name.drawHoverOverlay")
//    var drawHoverOverlay: Boolean = true
//
//    @field:Configurable(name = "ldlib.gui.editor.name.drawHoverTips")
//    var drawHoverTips: Boolean = true
//
//    @field:Configurable(name = "ldlib.gui.editor.name.fillDirection")
//    var fillDirection: ProgressTexture.FillDirection = ProgressTexture.FillDirection.ALWAYS_FULL
//
//    protected var onAddedTooltips: BiConsumer<ChemicalTankWidget<CHEMICAL, STACK>, List<Component>>? = null
//
//    var ingredientIO: IngredientIO = IngredientIO.RENDER_ONLY
//    var XEIChance: Float = 1f
//
//    protected var lastChemicalInTank: ChemicalStack<CHEMICAL>? = null
//    protected var lastTankCapacity: Long = 0
//
//    protected var changeListener: Runnable? = null
//
//    /* ---------------- constructors ---------------- */
//
//    constructor() : this(null, 0, 0, true, true)
//
//    constructor(
//        chemicalHandler: IChemicalHandler<CHEMICAL, STACK>?,
//        x: Int,
//        y: Int,
//        allowClickContainerFilling: Boolean,
//        allowClickContainerEmptying: Boolean
//    ) : this(
//        chemicalHandler, 0, x, y, 18, 18, allowClickContainerFilling, allowClickContainerEmptying
//    )
//
//    constructor(
//        chemicalHandler: IChemicalHandler<CHEMICAL, STACK>?,
//        x: Int,
//        y: Int,
//        width: Int,
//        height: Int,
//        allowClickContainerFilling: Boolean,
//        allowClickContainerEmptying: Boolean
//    ) : this(
//        chemicalHandler, 0, x, y, width, height, allowClickContainerFilling, allowClickContainerEmptying
//    )
//
//    constructor(
//        chemicalHandler: IChemicalHandler<CHEMICAL, STACK>?,
//        tank: Int,
//        x: Int,
//        y: Int,
//        allowClickContainerFilling: Boolean,
//        allowClickContainerEmptying: Boolean
//    ) : this(
//        chemicalHandler, tank, x, y, 18, 18, allowClickContainerFilling, allowClickContainerEmptying
//    )
//
//    constructor(
//        chemicalHandler: IChemicalHandler<CHEMICAL, STACK>?,
//        tank: Int,
//        x: Int,
//        y: Int,
//        width: Int,
//        height: Int,
//        allowClickContainerFilling: Boolean,
//        allowClickContainerEmptying: Boolean
//    ) : super(Position(x, y), Size(width, height)) {
//        this.chemicalHandler = chemicalHandler
//        this.tank = tank
//        this.showAmount = true
//        this.allowClickFilled = allowClickContainerFilling
//        this.allowClickDrained = allowClickContainerEmptying
//        this.drawHoverTips = true
//    }
//
//
//    override fun initTemplate() {
//        setBackground(FLUID_SLOT_TEXTURE)
//        fillDirection = ProgressTexture.FillDirection.DOWN_TO_UP
//    }
//
//    abstract fun getCapability(): Capability<out IChemicalHandler<CHEMICAL, STACK>>
//    abstract fun readStack(tag: CompoundTag?): ChemicalStack<CHEMICAL>
//
//    fun setChemicalTank(chemicalHandler: IChemicalHandler<CHEMICAL, STACK>): ChemicalTankWidget<CHEMICAL, STACK> {
//        this.chemicalHandler = chemicalHandler
//        this.tank = 0
//        if (isClientSideWidget) {
//            setClientSideWidget()
//        }
//        return this
//    }
//
//    fun setChemicalTank(
//        chemicalHandler: IChemicalHandler<CHEMICAL, STACK>, tank: Int
//    ): ChemicalTankWidget<CHEMICAL, STACK> {
//        this.chemicalHandler = chemicalHandler
//        this.tank = tank
//        if (isClientSideWidget) {
//            setClientSideWidget()
//        }
//        return this
//    }
//
//
//    override fun setClientSideWidget(): ChemicalTankWidget<CHEMICAL, STACK> {
//        super.setClientSideWidget()
//        lastChemicalInTank = chemicalHandler?.getChemicalInTank(tank)?.copy()
//        lastTankCapacity = chemicalHandler?.getTankCapacity(tank) ?: 0L
//        return this
//    }
//
//    // fluent api is shit
//    fun setBackground(background: IGuiTexture?): ChemicalTankWidget<CHEMICAL, STACK> {
//        super.setBackground(background)
//        return this
//    }
//
//    override fun getXEIIngredients(): List<Any?>? {
//        if (lastChemicalInTank == null || lastChemicalInTank!!.isEmpty) return emptyList()
//
//        if (GTCEu.Mods.isJEILoaded()) {
//            val jeiIngredient = JEIPlugin.jeiHelpers.ingredientManager.createTypedIngredient(lastChemicalInTank)
//                .map { ClickableIngredient(it, position.x, position.y, size.width, size.height) }
//            jeiIngredient.orElse(null)?.let { return listOf(it) }
//
//
//        } else if (GTCEu.Mods.isREILoaded()) {
//            return listOf(EntryStacks.of(TankWidget.REICallWrapper.toREIStack(lastFluidInTank)))
//        } else if (GTCEu.Mods.isEMILoaded()) {
//            return java.util.List.of(ForgeEmiStack.of(lastFluidInTank).setChance(XEIChance))
//        }
//        return java.util.List.of<Any?>(lastFluidInTank)
//    }
//
//
//}
