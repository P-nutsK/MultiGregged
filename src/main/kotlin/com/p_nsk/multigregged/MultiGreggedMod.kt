package com.p_nsk.multigregged

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import com.gregtechceu.gtceu.api.sound.SoundEntry
import com.p_nsk.multigregged.datagen.MGDDatagen
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("removal", "DEPRECATION")
@Mod(MultiGreggedMod.MOD_ID)
class MultiGreggedMod {

    companion object {
        const val MOD_ID: String = "multigregged"
        val LOGGER: Logger = LogManager.getLogger()
        var REGISTRATE: GTRegistrate = GTRegistrate.create(MOD_ID)

        /**
         * Create a ResourceLocation in the format "modid:path"
         *
         * @param path
         * @return ResourceLocation with the namespace of your mod
         */
        @JvmStatic fun id(path: String): ResourceLocation {
            return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
        }
    }

    init {
        val modEventBus = FMLJavaModLoadingContext.get().modEventBus

        modEventBus.addListener { event: FMLCommonSetupEvent? -> this.commonSetup(event!!) }
        modEventBus.addListener { event: FMLClientSetupEvent? -> this.clientSetup(event) }

        modEventBus.addListener { event: MaterialRegistryEvent? ->
            this.addMaterialRegistries(
                event
            )
        }
        modEventBus.addListener { event: MaterialEvent? -> this.addMaterials(event) }
        modEventBus.addListener { event: PostMaterialEvent? -> this.modifyMaterials(event) }

        modEventBus.addGenericListener(
            GTRecipeType::class.java
        ) { event: GTCEuAPI.RegisterEvent<ResourceLocation?, GTRecipeType?>? ->
            this.registerRecipeTypes(event)
        }
        modEventBus.addGenericListener(
            MachineDefinition::class.java
        ) { event: GTCEuAPI.RegisterEvent<ResourceLocation?, MachineDefinition?>? ->
            this.registerMachines(event)
        }
        modEventBus.addGenericListener(
            SoundEntry::class.java
        ) { event: GTCEuAPI.RegisterEvent<ResourceLocation?, SoundEntry?>? -> this.registerSounds(event) }

        // Most other events are fired on Forge's bus.
        // If we want to use annotations to register event listeners,
        // we need to register our object like this!
        MinecraftForge.EVENT_BUS.register(this)
        MGDConfigHolder.init()

        REGISTRATE.registerRegistrate()

        MGDDatagen.initPost()
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        event.enqueueWork {
            LOGGER.info("Hello from common setup! This is *after* registries are done, so we can do this:")
            LOGGER.info("Look, I found a {}!", Items.DIAMOND)
        }
    }

    private fun clientSetup(event: FMLClientSetupEvent?) {
        LOGGER.info("Hey, we're on Minecraft version {}!", Minecraft.getInstance().launchedVersion)
    }

    /**
     * Create a material manager for your mod using GT's API.
     * You MUST have this if you have custom materials.
     * Remember to register them not to GT's namespace, but your own.
     *
     * @param event
     */
    private fun addMaterialRegistries(event: MaterialRegistryEvent?) {
        GTCEuAPI.materialManager.createRegistry(MOD_ID)
    }

    /**
     * You will also need this for registering custom materials
     * Call init() from your Material class(es) here
     *
     * @param event
     */
    private fun addMaterials(event: MaterialEvent?) {
        // CustomMaterials.init();
    }

    /**
     * (Optional) Used to modify pre-existing materials from GregTech
     *
     * @param event
     */
    private fun modifyMaterials(event: PostMaterialEvent?) {
        // CustomMaterials.modify();
    }

    /**
     * Used to register your own new RecipeTypes.
     * Call init() from your RecipeType class(es) here
     *
     * @param event
     */
    private fun registerRecipeTypes(event: GTCEuAPI.RegisterEvent<ResourceLocation?, GTRecipeType?>?) {
        // CustomRecipeTypes.init();
    }

    /**
     * Used to register your own new machines.
     * Call init() from your Machine class(es) here
     *
     * @param event
     */
    private fun registerMachines(event: GTCEuAPI.RegisterEvent<ResourceLocation?, MachineDefinition?>?) {
        MGDMachines.init();
    }

    /**
     * Used to register your own new sounds
     * Call init from your Sound class(es) here
     *
     * @param event
     */
    fun registerSounds(event: GTCEuAPI.RegisterEvent<ResourceLocation?, SoundEntry?>?) {
        // CustomSounds.init();
    }

}