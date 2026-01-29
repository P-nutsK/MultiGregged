package com.p_nsk.multigregged.datagen

import com.tterrag.registrate.providers.RegistrateLangProvider

object LangHandler {
    fun init(provider: RegistrateLangProvider) {
        xei(provider)
        tooltip(provider)
        gui(provider)
        provider.add("recipe.capability.source.name", "Source")
        provider.add("recipe.capability.bonk.name", "Bonk")
    }

    fun xei(provider: RegistrateLangProvider) {
        provider.add("multigregged.recipe.bonk.in", "Bonk Input: %d")
        provider.add("multigregged.recipe.source.in", "Input Source: %d")
        provider.add("multigregged.recipe.source.in.tick", "Input Source: %d (per tick)")
        provider.add("multigregged.recipe.source.out", "Output Source: %d")
        provider.add("multigregged.recipe.source.out.tick", "Output Source: %d (per tick)")
    }

    fun tooltip(provider: RegistrateLangProvider) {
        provider.add("multigregged.machine.bonk_hatch.import.tooltip", "Bonk Input for Multiblocks")
        provider.add("multigregged.machine.source_hatch.import.tooltip", "Source Input for Multiblocks")
        provider.add("multigregged.machine.source_hatch.export.tooltip", "Source Output for Multiblocks")
        provider.add("multigregged.universal.tooltip.bonk_storage_capacity", "§9Bonk Capacity: §f%d Bonk")
        provider.add("multigregged.universal.tooltip.source_storage_capacity", "§9Source Capacity: §f%d Source")
        provider.add("multigregged.source.amount", "§9Amount: %d/%d")

    }

    fun gui(provider: RegistrateLangProvider) {
        provider.add("multigregged.gui.source", "Source:")
    }
}