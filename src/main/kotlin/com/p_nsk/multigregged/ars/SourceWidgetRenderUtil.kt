package com.p_nsk.multigregged.ars

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.InventoryMenu

object SourceWidgetRenderUtil {
    fun drawSourceLikeFluid(
        graphics: GuiGraphics,
        sprite: TextureAtlasSprite,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Int = 0xFFFFFFFF.toInt()
    ) {
        if (width <= 0f || height <= 0f) return

        // 16px タイル前提
        val xTileCount = (width / 16f).toInt()
        val xRemainder = width - xTileCount * 16f
        val yTileCount = (height / 16f).toInt()
        val yRemainder = height - yTileCount * 16f

        val yStart = y + height

        RenderSystem.enableBlend()
        RenderSystem.setShaderTexture(0,  InventoryMenu.BLOCK_ATLAS);

        for (xTile in 0..xTileCount) {
            for (yTile in 0..yTileCount) {
                val tileWidth =
                    if (xTile == xTileCount) xRemainder else 16f
                val tileHeight =
                    if (yTile == yTileCount) yRemainder else 16f

                if (tileWidth <= 0f || tileHeight <= 0f) continue

                DrawerHelper.drawFluidTexture(
                    graphics,
                    x + xTile * 16f,
                    yStart - (yTile + 1) * 16f,
                    sprite,
                    16f - tileHeight, // maskTop
                    16f - tileWidth,  // maskRight
                    0f,
                    color
                )
            }
        }
    }
    fun getSprite(texture: ResourceLocation): TextureAtlasSprite {
        return Minecraft.getInstance()
            .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(texture)
    }
}
