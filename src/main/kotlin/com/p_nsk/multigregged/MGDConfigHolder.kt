package com.p_nsk.multigregged

import com.gregtechceu.gtceu.GTCEu
import dev.toma.configuration.Configuration
import dev.toma.configuration.config.Config
import dev.toma.configuration.config.Configurable
import dev.toma.configuration.config.format.ConfigFormats

@Config(id = MultiGreggedMod.MOD_ID)
class ConfigHolder {
    @Configurable
    var features = FeatureConfig()

    class FeatureConfig {
        @Configurable
        @Configurable.Comment(
            "Whether to enable the hatch that accepts Ars Nouveau's source",
            "Ars Énergistique is required.",
            "Default: true"
        )
        var sourceHatch: Boolean = true // default false
        fun sourceHatchEnabled(): Boolean {
            return sourceHatch && GTCEu.isModLoaded("ars_nouveau") &&
                    GTCEu.isModLoaded("arseng")
        }

        @Configurable
        @Configurable.Comment(
            "Whether to generate Bonk hatches that hold the number of times a hatch was right-clicked with a hammer.",
            "Default: true"
        )
        var bonkHatch: Boolean = true // default true

    }

    companion object {
        lateinit var INSTANCE: ConfigHolder
        private val LOCK = Any()

        fun init() {
            synchronized(LOCK) {
                if (INSTANCE == null) {
                    INSTANCE =
                        Configuration.registerConfig(ConfigHolder::class.java, ConfigFormats.yaml())
                            .getConfigInstance()
                }
            }
        }
    }
}