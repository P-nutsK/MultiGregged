package com.p_nsk.multigregged

import com.gregtechceu.gtceu.GTCEu
import dev.toma.configuration.Configuration
import dev.toma.configuration.config.Config
import dev.toma.configuration.config.Configurable
import dev.toma.configuration.config.format.ConfigFormats

@Config(id = MultiGreggedMod.MOD_ID)
class MGDConfigHolder {
    @Configurable
    var features = FeatureConfig()

    class FeatureConfig {
        @Configurable
        @Configurable.Comment(
            "Whether to enable the hatch that accepts Ars Nouveau's source",
            "Ars Énergistique is required.",
            "Default: true"
        )
        var sourceHatch: Boolean = true
        fun sourceHatchAvailable(): Boolean {
            return sourceHatch && GTCEu.isModLoaded("ars_nouveau") &&
                    GTCEu.isModLoaded("arseng")
        }

        @Configurable
        @Configurable.Comment(
            "Whether to generate Bonk hatches that hold the number of times a hatch was right-clicked with a hammer.",
            "Default: true"
        )
        var bonkHatch: Boolean = true

    }

    companion object {
        lateinit var INSTANCE: MGDConfigHolder
        private val LOCK = Any()

        fun init() {
            synchronized(LOCK) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE =
                        Configuration.registerConfig(MGDConfigHolder::class.java, ConfigFormats.yaml())
                            .getConfigInstance()
                }
            }
        }
    }
}