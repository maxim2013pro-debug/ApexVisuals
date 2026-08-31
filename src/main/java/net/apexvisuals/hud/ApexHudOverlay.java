package net.apexvisuals.hud;

import net.apexvisuals.config.ColorPalette;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class ApexHudOverlay implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options.hudHidden) return;

        TextRenderer tr = client.textRenderer;
        int fps = client.getCurrentFps();
        String text = "APEX " + fps + " FPS";

        int x = 10;
        int y = 10;
        int width = tr.getWidth(text) + 12;
        int height = 18;

        drawContext.fill(x, y, x + width, y + height, ColorPalette.BACKGROUND_BLACK);
        drawContext.fill(x, y, x + 2, y + height, ColorPalette.PRIMARY_RED);
        drawContext.drawText(tr, text, x + 7, y + 5, ColorPalette.TEXT_WHITE, true);
    }
}
