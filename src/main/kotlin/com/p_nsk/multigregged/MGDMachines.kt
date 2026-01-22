package com.p_nsk.multigregged

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils
import com.gregtechceu.gtceu.common.data.models.GTMachineModels.OVERLAY_ITEM_HATCH_INPUT
import com.gregtechceu.gtceu.utils.FormattingUtil
import com.p_nsk.multigregged.MGDMachineUtils.registerTieredMachines
import com.p_nsk.multigregged.bonk.BonkHatchPartMachine
import net.minecraft.network.chat.Component
import java.util.function.BiFunction
import com.p_nsk.multigregged.MultiGreggedMod.Companion.REGISTRATE
import com.p_nsk.multigregged.ars.ArsMachines

@Suppress("unused")
object MGDMachines {
    private const val BONK_INITIAL_CAPACITY = 8;
    val BONK_INPUT_HATCH = registerTieredMachines(
        "bonk_input_hatch",
        { holder, tier -> BonkHatchPartMachine(holder, tier, IO.IN, BONK_INITIAL_CAPACITY) },
        { tier, builder ->
            builder.langValue(GTValues.VNF[tier] + " Bonk Input Hatch")
                .abilities(MGDPartAbilities.INPUT_BONK)
                .rotationState(RotationState.ALL)
                .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                .colorOverlayTieredHullModel(
                    GTCEu.id("block/overlay/machine/$OVERLAY_ITEM_HATCH_INPUT"),
                    null,
                    GTCEu.id("block/overlay/machine/overlay_pipe_in_emissive")
                )
                .tooltips(Component.translatable("tooltip.multigregged.bonk_input_hatch"))
                .tooltips(
                    Component.translatable(
                        "multigregged.universal.tooltip.bonk_storage_capacity",
                        FormattingUtil
                            .formatNumbers(BonkHatchPartMachine.getBonkCapacity(BONK_INITIAL_CAPACITY, tier))
                    )
                ).register()
        }, *GTMachineUtils.ALL_TIERS
    )


    fun init() {
        // Ars IntegrationはArsEngが有効な場合にのみ
        if(GTCEu.isModLoaded("arseng")) {
            ArsMachines.init()
        }
    }
}