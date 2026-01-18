package com.p_nsk.multigregged.datagen

import com.tterrag.registrate.providers.RegistrateLangProvider

object LangHandler {
    fun init(provider: RegistrateLangProvider) {
        provider.add("gtceu.machine.bonk_hatch.import.tooltip", "Bonk Input for Multiblocks")
        provider.add("multigregged.universal.tooltip.bonk_storage_capacity", "§9Bonk Capacity: §f%d Bonk")
    }
}