package com.p_nsk.multigregged.datagen

import com.tterrag.registrate.providers.RegistrateLangProvider

object LangHandler {
    fun init(provider: RegistrateLangProvider) {
        xei(provider)
        tooltip(provider)
        gui(provider)
    }
    fun xei(provider: RegistrateLangProvider) {
        provider.add("multigregged.recipe.source.in","Input Source: %s")
        provider.add("multigregged.recipe.source.in.tick","Input Source: %s (per tick)")
        provider.add("multigregged.recipe.source.out","Output Source: %s")
        provider.add("multigregged.recipe.source.out.tick","Output Source: %s (per tick)")
    }
    fun tooltip(provider: RegistrateLangProvider) {
        provider.add("gtceu.machine.bonk_hatch.import.tooltip", "Bonk Input for Multiblocks")
        provider.add("multigregged.universal.tooltip.bonk_storage_capacity", "§9Bonk Capacity: §f%d Bonk")
        provider.add("gtceu.machine.source_hatch.import.tooltip", "Source Input for Multiblocks")
        provider.add("gtceu.machine.source_hatch.export.tooltip", "Source Output for Multiblocks")
        provider.add("multigregged.universal.tooltip.source_storage_capacity", "§9Source Capacity: §f%d Source")
    }
    fun gui(provider: RegistrateLangProvider) {
        provider.add("multigregged.gui.source", "Source:")
    }
}