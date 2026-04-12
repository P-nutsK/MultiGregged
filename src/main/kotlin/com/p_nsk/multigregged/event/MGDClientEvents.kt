package com.p_nsk.multigregged.event

import com.gregtechceu.gtceu.GTCEu
import com.p_nsk.multigregged.MGDConfigHolder
import net.minecraft.network.chat.Component
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingIn
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod


@Mod.EventBusSubscriber(value = [Dist.CLIENT])
class MGDClientEvents {
    @SubscribeEvent
    fun onLogin(event: LoggingIn) {
        val player = event.player ?: return

        if (MGDConfigHolder.INSTANCE.features.sourceHatch && GTCEu.isModLoaded("ars_nouveau") &&
            !GTCEu.isModLoaded("arseng")
        ) {
            player.sendSystemMessage(
                Component.literal("⚠️ 警告: ソースハッチが有効化されていますがArs Énergistiqueがインストールされていません。ソースハッチにはArs Énergistiqueが必要です。")
            )
        }
    }
}