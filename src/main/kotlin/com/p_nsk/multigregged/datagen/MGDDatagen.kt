package com.p_nsk.multigregged.datagen

import com.p_nsk.multigregged.MultiGreggedMod.Companion.REGISTRATE
import com.tterrag.registrate.providers.ProviderType

object MGDDatagen {
    fun initPost() {
        REGISTRATE.addDataGenerator(ProviderType.LANG, LangHandler::init)
    }
}