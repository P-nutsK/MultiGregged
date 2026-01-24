package com.p_nsk.multigregged.ars

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils
import com.gregtechceu.gtceu.common.data.models.GTMachineModels
import com.gregtechceu.gtceu.utils.FormattingUtil
import com.p_nsk.multigregged.MGDMachineUtils
import com.p_nsk.multigregged.MGDPartAbilities
import net.minecraft.network.chat.Component

object ArsMachines {
    //TODO configurable
    private const val INITIAL_SOURCE_CAPACITY = 1000
    val SOURCE_INPUT_HATCH = registerSourceHatch(
        "source_input_hatch", "Source Input Hatch", IO.IN
    )
    val SOURCE_OUTPUT_HATCH = registerSourceHatch(
        "source_output_hatch", "Source Output Hatch", IO.OUT
    )

    fun registerSourceHatch(name: String, displayName: String, io: IO): Array<MachineDefinition> {
        val ioOverlay =
            if (io == IO.OUT) GTMachineModels.OVERLAY_FLUID_HATCH_OUTPUT else GTMachineModels.OVERLAY_FLUID_HATCH_INPUT
        val emissiveOverlay = if (io == IO.OUT) "overlay_pipe_out_emissive" else "overlay_pipe_in_emissive"
        val tooltip = if (io == IO.OUT) "source_hatch.export" else "source_hatch.import"
        val ability = if (io == IO.OUT) MGDPartAbilities.OUTPUT_SOURCE else MGDPartAbilities.INPUT_SOURCE

        return MGDMachineUtils.registerTieredMachines(
            name, { holder, tier ->
                SourceHatchPartMachine(
                    holder as MetaMachineBlockEntity, tier, io, INITIAL_SOURCE_CAPACITY
                )
            }, { tier, builder ->
                builder.langValue("${GTValues.VNF[tier]} $displayName").abilities(ability)
                    .rotationState(RotationState.ALL)
                    .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                    .colorOverlayTieredHullModel(
                        GTCEu.id("block/overlay/machine/$ioOverlay"),
                        null,
                        GTCEu.id("block/overlay/machine/$emissiveOverlay"),
                    ).tooltips(Component.translatable("multigregged.machine.$tooltip")).tooltips(
                        Component.translatable(
                            "multigregged.universal.tooltip.source_storage_capacity",
                            FormattingUtil.formatNumbers(SourceHatchPartMachine.getMaxCapacity(1000, tier))
                        )
                    ).register()
            }, ::SourceMachineBlockEntity, *GTMachineUtils.ALL_TIERS
        )
    }

    fun init() {}
}