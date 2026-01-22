package com.p_nsk.multigregged

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.block.MetaMachineBlock
import com.gregtechceu.gtceu.api.item.MetaMachineItem
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils
import com.p_nsk.multigregged.MultiGreggedMod.Companion.REGISTRATE
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import org.apache.commons.lang3.function.TriFunction
import java.util.function.BiFunction
import java.util.function.Function

object MGDMachineUtils {
    fun registerTieredMachines(
        name: String,
        factory: BiFunction<IMachineBlockEntity, Int, MetaMachine>,
        builder: BiFunction<Int, MachineBuilder<MachineDefinition>, MachineDefinition>,
        vararg tiers: Int
    ): Array<MachineDefinition> {
        return GTMachineUtils.registerTieredMachines(REGISTRATE, name, factory, builder, *tiers)
    }

    fun registerTieredMachines(
        name: String,
        factory: BiFunction<IMachineBlockEntity, Int, MetaMachine>,
        builder: BiFunction<Int, MachineBuilder<MachineDefinition>, MachineDefinition>,
        blockEntity: TriFunction<BlockEntityType<*>, BlockPos, BlockState, IMachineBlockEntity>,
        vararg tiers: Int
    ): Array<MachineDefinition> {
        val definitions = arrayOfNulls<MachineDefinition>(GTValues.TIER_COUNT)
        for (tier in tiers) {
            val register = registerBlockEntityMachine(
                GTValues.VN[tier].lowercase() + "_" + name, { holder -> factory.apply(holder, tier) }, blockEntity
            ).tier(tier)
            definitions[tier] = builder.apply(tier, register)
        }
        return definitions.requireNoNulls()
    }


    fun registerBlockEntityMachine(
        name: String,
        machineFactory: Function<IMachineBlockEntity, MetaMachine>,
        blockEntityFactory: TriFunction<BlockEntityType<*>, BlockPos, BlockState, IMachineBlockEntity>,
    ): MachineBuilder<MachineDefinition> {
        return MachineBuilder(
            REGISTRATE,
            name,
            ::MachineDefinition,
            machineFactory,
            ::MetaMachineBlock,
            ::MetaMachineItem,
            blockEntityFactory
        )
    }
}