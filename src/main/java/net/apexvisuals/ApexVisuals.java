package net.apexvisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ApexVisuals implements ClientModInitializer {
    public static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.apexvisuals.open_menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "Apex Visuals"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    client.setScreen(new ApexClickGui());
                }
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ApexConfig.hudEnabled) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options.hudHidden) return;

            drawContext.fill(10, 10, 145, 32, 0xDD101010);
            drawContext.fill(10, 10, 13, 32, 0xFFFF1E27);

            drawContext.drawText(client.textRenderer, "APEX", 18, 16, 0xFFFF1E27, true);
            drawContext.drawText(client.textRenderer, "VISUALS", 45, 16, 0xFFFFFFFF, true);

            if (ApexConfig.showFps) {
                int fps = client.getCurrentFps();
                drawContext.drawText(client.textRenderer, "FPS: §f" + fps, 100, 16, 0xAA888888, true);
            }
        });
    }

    // --- КОНФИГУРАЦИЯ ---
    public static class ApexConfig {
        public static boolean oldBlockAnimation = true;
        public static boolean smoothSwing = true;
        public static boolean customTransformEnabled = true;

        public static float mainHandX = 0.0f;
        public static float mainHandY = 0.0f;
        public static float mainHandZ = 0.0f;
        public static float itemScale = 1.0f;

        public static boolean disableHurtCam = true;
        public static boolean hudEnabled = true;
        public static boolean showFps = true;
    }

    // --- CLICK GUI С БЛЮРОМ И АНИМАЦИЕЙ ---
    public static class ApexClickGui extends Screen {
        private int selectedTab = 0;
        private final String[] tabs = {"Animations", "Position", "Render"};

        private boolean draggingScale = false;
        private boolean draggingX = false;
        private boolean draggingY = false;
        private boolean draggingZ = false;

        private float animProgress = 0.0f;
        private long lastTime = System.currentTimeMillis();

        public ApexClickGui() {
            super(Text.literal("Apex Visuals"));
        }

        @Override
        protected void init() {
            this.animProgress = 0.0f;
            this.lastTime = System.currentTimeMillis();

            if (this.client != null && this.client.gameRenderer != null) {
                try {
                    this.client.gameRenderer.loadPostProcessor(new Identifier("shaders/post/blur.json"));
                } catch (Exception ignored) {}
            }
        }

        @Override
        public void close() {
            if (this.client != null && this.client.gameRenderer != null) {
                this.client.gameRenderer.disablePostProcessor();
            }
            super.close();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - lastTime) / 1000.0f;
            lastTime = currentTime;

            if (animProgress < 1.0f) {
                animProgress += deltaTime * 8.0f;
                if (animProgress > 1.0f) animProgress = 1.0f;
            }

            float smoothFactor = 1.0f - (float) Math.pow(1.0f - animProgress, 3);

            int bgAlpha = (int) (160 * smoothFactor);
            context.fill(0, 0, this.width, this.height, (bgAlpha << 24));

            int guiX = (this.width - 360) / 2;
            int guiY = (this.height - 220) / 2;

            context.getMatrices().push();
            float scale = 0.85f + (0.15f * smoothFactor);
            float centerX = this.width / 2.0f;
            float centerY = this.height / 2.0f;

            context.getMatrices().translate(centerX, centerY, 0);
            context.getMatrices().scale(scale, scale, 1.0f);
            context.getMatrices().translate(-centerX, -centerY, 0);

            int unscaledMouseX = (int) ((mouseX - centerX) / scale + centerX);
            int unscaledMouseY = (int) ((mouseY - centerY) / scale + centerY);

            context.fill(guiX, guiY, guiX + 360, guiY + 220, 0xEF0F0F0F);
            context.fill(guiX, guiY, guiX + 100, guiY + 220, 0xEF161616);
            context.fill(guiX, guiY, guiX + 360, guiY + 3, 0xFFFF1E27);

            context.drawText(this.textRenderer, "APEX", guiX + 15, guiY + 12, 0xFFFF1E27, true);
            context.drawText(this.textRenderer, "VISUALS", guiX + 48, guiY + 12, 0xFFFFFFFF, true);

            for (int i = 0; i < tabs.length; i++) {
                int tabY = guiY + 45 + (i * 28);
                boolean hovered = unscaledMouseX >= guiX && unscaledMouseX <= guiX + 100 && unscaledMouseY >= tabY && unscaledMouseY <= tabY + 22;
                boolean active = selectedTab == i;

                if (active) {
                    context.fill(guiX, tabY, guiX + 100, tabY + 22, 0x25FF1E27);
                    context.fill(guiX, tabY, guiX + 3, tabY + 22, 0xFFFF1E27);
                } else if (hovered) {
                    context.fill(guiX, tabY, guiX + 100, tabY + 22, 0x11FFFFFF);
                }

                context.drawText(this.textRenderer, tabs[i], guiX + 15, tabY + 7, active ? 0xFFFF1E27 : 0xAA888888, false);
            }

            int contentX = guiX + 115;
            int contentY = guiY + 45;

            if (selectedTab == 0) {
                drawToggle(context, contentX, contentY, "1.7 Sword Block", ApexConfig.oldBlockAnimation);
                drawToggle(context, contentX, contentY + 30, "Smooth Swing", ApexConfig.smoothSwing);
                drawToggle(context, contentX, contentY + 60, "Enable Position Logic", ApexConfig.customTransformEnabled);
            } else if (selectedTab == 1) {
                drawSlider(context, contentX, contentY, "Item Scale", ApexConfig.itemScale, 0.5f, 2.0f, unscaledMouseX, unscaledMouseY, draggingScale);
                drawSlider(context, contentX, contentY + 35, "MainHand X", ApexConfig.mainHandX, -1.0f, 1.0f, unscaledMouseX, unscaledMouseY, draggingX);
                drawSlider(context, contentX, contentY + 70, "MainHand Y", ApexConfig.mainHandY, -1.0f, 1.0f, unscaledMouseX, unscaledMouseY, draggingY);
                drawSlider(context, contentX, contentY + 105, "MainHand Z", ApexConfig.mainHandZ, -1.0f, 1.0f, unscaledMouseX, unscaledMouseY, draggingZ);
            } else if (selectedTab == 2) {
                drawToggle(context, contentX, contentY, "Disable HurtCam", ApexConfig.disableHurtCam);
                drawToggle(context, contentX, contentY + 30, "Show HUD Overlay", ApexConfig.hudEnabled);
                drawToggle(context, contentX, contentY + 60, "Show FPS Indicator", ApexConfig.showFps);
            }

            context.getMatrices().pop();

            super.render(context, mouseX, mouseY, delta);
        }

        private void drawToggle(DrawContext context, int x, int y, String label, boolean enabled) {
            context.drawText(this.textRenderer, label, x, y + 4, 0xFFFFFFFF, false);
            int buttonX = x + 170;
            context.fill(buttonX, y, buttonX + 36, y + 16, enabled ? 0xFFFF1E27 : 0xFF2A2A2A);
            context.fill(enabled ? buttonX + 19 : buttonX + 2, y + 2, enabled ? buttonX + 34 : buttonX + 17, y + 14, 0xFFFFFFFF);
        }

        private void drawSlider(DrawContext context, int x, int y, String label, float val, float min, float max, int mouseX, int mouseY, boolean isDragging) {
            context.drawText(this.textRenderer, label + ": §7" + String.format("%.2f", val), x, y, 0xFFFFFFFF, false);
            int trackX = x;
            int trackY = y + 12;
            int trackWidth = 206;

            context.fill(trackX, trackY, trackX + trackWidth, trackY + 6, 0xFF252525);

            float pct = (val - min) / (max - min);
            int fillW = (int) (trackWidth * pct);
            context.fill(trackX, trackY, trackX + fillW, trackY + 6, 0xFFFF1E27);
            context.fill(trackX + fillW - 2, trackY - 2, trackX + fillW + 4, trackY + 8, 0xFFFFFFFF);

            if (isDragging) {
                float newPct = Math.max(0.0f, Math.min(1.0f, (float)(mouseX - trackX) / trackWidth));
                float newVal = min + newPct * (max - min);
                if (label.contains("Scale")) ApexConfig.itemScale = newVal;
                if (label.contains("X")) ApexConfig.mainHandX = newVal;
                if (label.contains("Y")) ApexConfig.mainHandY = newVal;
                if (label.contains("Z")) ApexConfig.mainHandZ = newVal;
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            float smoothFactor = 1.0f - (float) Math.pow(1.0f - animProgress, 3);
            float scale = 0.85f + (0.15f * smoothFactor);
            float centerX = this.width / 2.0f;
            float centerY = this.height / 2.0f;

            int unscaledMouseX = (int) ((mouseX - centerX) / scale + centerX);
            int unscaledMouseY = (int) ((mouseY - centerY) / scale + centerY);

            int guiX = (this.width - 360) / 2;
            int guiY = (this.height - 220) / 2;

            if (unscaledMouseX >= guiX && unscaledMouseX <= guiX + 100) {
                for (int i = 0; i < tabs.length; i++) {
                    int tabY = guiY + 45 + (i * 28);
                    if (unscaledMouseY >= tabY && unscaledMouseY <= tabY + 22) {
                        this.selectedTab = i;
                        return true;
                    }
                }
            }

            int contentX = guiX + 115;
            int contentY = guiY + 45;

            if (selectedTab == 0 && unscaledMouseX >= contentX + 170 && unscaledMouseX <= contentX + 206) {
                if (unscaledMouseY >= contentY && unscaledMouseY <= contentY + 16) ApexConfig.oldBlockAnimation = !ApexConfig.oldBlockAnimation;
                if (unscaledMouseY >= contentY + 30 && unscaledMouseY <= contentY + 46) ApexConfig.smoothSwing = !ApexConfig.smoothSwing;
                if (unscaledMouseY >= contentY + 60 && unscaledMouseY <= contentY + 76) ApexConfig.customTransformEnabled = !ApexConfig.customTransformEnabled;
            } else if (selectedTab == 1 && unscaledMouseX >= contentX && unscaledMouseX <= contentX + 206) {
                if (unscaledMouseY >= contentY + 10 && unscaledMouseY <= contentY + 22) draggingScale = true;
                if (unscaledMouseY >= contentY + 45 && unscaledMouseY <= contentY + 57) draggingX = true;
                if (unscaledMouseY >= contentY + 80 && unscaledMouseY <= contentY + 92) draggingY = true;
                if (unscaledMouseY >= contentY + 115 && unscaledMouseY <= contentY + 127) draggingZ = true;
            } else if (selectedTab == 2 && unscaledMouseX >= contentX + 170 && unscaledMouseX <= contentX + 206) {
                if (unscaledMouseY >= contentY && unscaledMouseY <= contentY + 16) ApexConfig.disableHurtCam = !ApexConfig.disableHurtCam;
                if (unscaledMouseY >= contentY + 30 && unscaledMouseY <= contentY + 46) ApexConfig.hudEnabled = !ApexConfig.hudEnabled;
                if (unscaledMouseY >= contentY + 60 && unscaledMouseY <= contentY + 76) ApexConfig.showFps = !ApexConfig.showFps;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            draggingScale = draggingX = draggingY = draggingZ = false;
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean shouldPause() { return false; }
    }
}package net.apexvisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ApexVisuals implements ClientModInitializer {
    public static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.apexvisuals.open_menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "Apex Visuals"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    client.setScreen(new ApexClickGui());
                }
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ApexConfig.hudEnabled) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options.hudHidden) return;

            drawContext.fill(10, 10, 145, 32, 0xDD101010);
            drawContext.fill(10, 10, 13, 32, 0xFFFF1E27);

            drawContext.drawText(client.textRenderer, "APEX", 18, 16, 0xFFFF1E27, true);
            drawContext.drawText(client.textRenderer, "VISUALS", 45, 16, 0xFFFFFFFF, true);

            if (ApexConfig.showFps) {
                int fps = client.getCurrentFps();
                drawContext.drawText(client.textRenderer, "FPS: §f" + fps, 100, 16, 0xAA888888, true);
            }
        });
    }

    // --- КОНФИГУРАЦИЯ ---
    public static class ApexConfig {
        public static boolean oldBlockAnimation = true;
        public static boolean smoothSwing = true;
        public static boolean customTransformEnabled = true;

        public static float mainHandX = 0.0f;
        public static float mainHandY = 0.0f;
        public static float mainHandZ = 0.0f;
        public static float itemScale = 1.0f;

        public static boolean disableHurtCam = true;
        public static boolean hudEnabled = true;
        public static boolean showFps = true;
    }

    // --- CLICK GUI С БЛЮРОМ И АНИМАЦИЕЙ ---
    public static class ApexClickGui extends Screen {
        private int selectedTab = 0;
        private final String[] tabs = {"Animations", "Position", "Render"};

        private boolean draggingScale = false;
        private boolean draggingX = false;
        private boolean draggingY = false;
        private boolean draggingZ = false;

        private float animProgress = 0.0f;
        private long lastTime = System.currentTimeMillis();

        public ApexClickGui() {
            super(Text.literal("Apex Visuals"));
        }

        @Override
        protected void init() {
            this.animProgress = 0.0f;
            this.lastTime = System.currentTimeMillis();

            if (this.client != null && this.client.gameRenderer != null) {
                try {
                    this.client.gameRenderer.loadPostProcessor(new Identifier("shaders/post/blur.json"));
                } catch (Exception ignored) {}
            }
        }

        @Override
        public void close() {
            if (this.client != null && this.client.gameRenderer != null) {
                this.client.gameRenderer.disablePostProcessor();
            }
            super.close();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - lastTime) / 1000.0f;
            lastTime = currentTime;

            if (animProgress < 1.0f) {
                animProgress += deltaTime * 8.0f;
                if (animProgress > 1.0f) animProgress = 1.0f;
            }

            float smoothFactor = 1.0f - (float) Math.pow(1.0f - animProgress, 3);

            int bgAlpha = (int) (160 * smoothFactor);
            context.fill(0, 0, this.width, this.height, (bgAlpha << 24));

            int guiX = (this.width - 360) / 2;
            int guiY = (this.height - 220) / 2;

            context.getMatrices().push();
            float scale = 0.85f + (0.15f * smoothFactor);
            float centerX = this.width / 2.0f;
            float centerY = this.height / 2.0f;

            context.getMatrices().translate(centerX, centerY, 0);
            context.getMatrices().scale(scale, scale, 1.0f);
            context.getMatrices().translate(-centerX, -centerY, 0);

            int unscaledMouseX = (int) ((mouseX - centerX) / scale + centerX);
            int unscaledMouseY = (int) ((mouseY - centerY) / scale + centerY);

            context.fill(guiX, guiY, guiX + 360, guiY + 220, 0xEF0F0F0F);
            context.fill(guiX, guiY, guiX + 100, guiY + 220, 0xEF161616);
            context.fill(guiX, guiY, guiX + 360, guiY + 3, 0xFFFF1E27);

            context.drawText(this.textRenderer, "APEX", guiX + 15, guiY + 12, 0xFFFF1E27, true);
            context.drawText(this.textRenderer, "VISUALS", guiX + 48, guiY + 12, 0xFFFFFFFF, true);

            for (int i = 0; i < tabs.length; i++) {
                int tabY = guiY + 45 + (i * 28);
                boolean hovered = unscaledMouseX >= guiX && unscaledMouseX <= guiX + 100 && unscaledMouseY >= tabY && unscaledMouseY <= tabY + 22;
                boolean active = selectedTab == i;

                if (active) {
                    context.fill(guiX, tabY, guiX + 100, tabY + 22, 0x25FF1E27);
                    context.fill(guiX, tabY, guiX + 3, tabY + 22, 0xFFFF1E27);
                } else if (hovered) {
                    context.fill(guiX, tabY, guiX + 100, tabY + 22, 0x11FFFFFF);
                }

                context.drawText(this.textRenderer, tabs[i], guiX + 15, tabY + 7, active ? 0xFFFF1E27 : 0xAA888888, false);
            }

            int contentX = guiX + 115;
            int contentY = guiY + 45;

            if (selectedTab == 0) {
                drawToggle(context, contentX, contentY, "1.7 Sword Block", ApexConfig.oldBlockAnimation);
                drawToggle(context, contentX, contentY + 30, "Smooth Swing", ApexConfig.smoothSwing);
                drawToggle(context, contentX, contentY + 60, "Enable Position Logic", ApexConfig.customTransformEnabled);
            } else if (selectedTab == 1) {
                drawSlider(context, contentX, contentY, "Item Scale", ApexConfig.itemScale, 0.5f, 2.0f, unscaledMouseX, unscaledMouseY, draggingScale);
                drawSlider(context, contentX, contentY + 35, "MainHand X", ApexConfig.mainHandX, -1.0f, 1.0f, unscaledMouseX, unscaledMouseY, draggingX);
                drawSlider(context, contentX, contentY + 70, "MainHand Y", ApexConfig.mainHandY, -1.0f, 1.0f, unscaledMouseX, unscaledMouseY, draggingY);
                drawSlider(context, contentX, contentY + 105, "MainHand Z", ApexConfig.mainHandZ, -1.0f, 1.0f, unscaledMouseX, unscaledMouseY, draggingZ);
            } else if (selectedTab == 2) {
                drawToggle(context, contentX, contentY, "Disable HurtCam", ApexConfig.disableHurtCam);
                drawToggle(context, contentX, contentY + 30, "Show HUD Overlay", ApexConfig.hudEnabled);
                drawToggle(context, contentX, contentY + 60, "Show FPS Indicator", ApexConfig.showFps);
            }

            context.getMatrices().pop();

            super.render(context, mouseX, mouseY, delta);
        }

        private void drawToggle(DrawContext context, int x, int y, String label, boolean enabled) {
            context.drawText(this.textRenderer, label, x, y + 4, 0xFFFFFFFF, false);
            int buttonX = x + 170;
            context.fill(buttonX, y, buttonX + 36, y + 16, enabled ? 0xFFFF1E27 : 0xFF2A2A2A);
            context.fill(enabled ? buttonX + 19 : buttonX + 2, y + 2, enabled ? buttonX + 34 : buttonX + 17, y + 14, 0xFFFFFFFF);
        }

        private void drawSlider(DrawContext context, int x, int y, String label, float val, float min, float max, int mouseX, int mouseY, boolean isDragging) {
            context.drawText(this.textRenderer, label + ": §7" + String.format("%.2f", val), x, y, 0xFFFFFFFF, false);
            int trackX = x;
            int trackY = y + 12;
            int trackWidth = 206;

            context.fill(trackX, trackY, trackX + trackWidth, trackY + 6, 0xFF252525);

            float pct = (val - min) / (max - min);
            int fillW = (int) (trackWidth * pct);
            context.fill(trackX, trackY, trackX + fillW, trackY + 6, 0xFFFF1E27);
            context.fill(trackX + fillW - 2, trackY - 2, trackX + fillW + 4, trackY + 8, 0xFFFFFFFF);

            if (isDragging) {
                float newPct = Math.max(0.0f, Math.min(1.0f, (float)(mouseX - trackX) / trackWidth));
                float newVal = min + newPct * (max - min);
                if (label.contains("Scale")) ApexConfig.itemScale = newVal;
                if (label.contains("X")) ApexConfig.mainHandX = newVal;
                if (label.contains("Y")) ApexConfig.mainHandY = newVal;
                if (label.contains("Z")) ApexConfig.mainHandZ = newVal;
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            float smoothFactor = 1.0f - (float) Math.pow(1.0f - animProgress, 3);
            float scale = 0.85f + (0.15f * smoothFactor);
            float centerX = this.width / 2.0f;
            float centerY = this.height / 2.0f;

            int unscaledMouseX = (int) ((mouseX - centerX) / scale + centerX);
            int unscaledMouseY = (int) ((mouseY - centerY) / scale + centerY);

            int guiX = (this.width - 360) / 2;
            int guiY = (this.height - 220) / 2;

            if (unscaledMouseX >= guiX && unscaledMouseX <= guiX + 100) {
                for (int i = 0; i < tabs.length; i++) {
                    int tabY = guiY + 45 + (i * 28);
                    if (unscaledMouseY >= tabY && unscaledMouseY <= tabY + 22) {
                        this.selectedTab = i;
                        return true;
                    }
                }
            }

            int contentX = guiX + 115;
            int contentY = guiY + 45;

            if (selectedTab == 0 && unscaledMouseX >= contentX + 170 && unscaledMouseX <= contentX + 206) {
                if (unscaledMouseY >= contentY && unscaledMouseY <= contentY + 16) ApexConfig.oldBlockAnimation = !ApexConfig.oldBlockAnimation;
                if (unscaledMouseY >= contentY + 30 && unscaledMouseY <= contentY + 46) ApexConfig.smoothSwing = !ApexConfig.smoothSwing;
                if (unscaledMouseY >= contentY + 60 && unscaledMouseY <= contentY + 76) ApexConfig.customTransformEnabled = !ApexConfig.customTransformEnabled;
            } else if (selectedTab == 1 && unscaledMouseX >= contentX && unscaledMouseX <= contentX + 206) {
                if (unscaledMouseY >= contentY + 10 && unscaledMouseY <= contentY + 22) draggingScale = true;
                if (unscaledMouseY >= contentY + 45 && unscaledMouseY <= contentY + 57) draggingX = true;
                if (unscaledMouseY >= contentY + 80 && unscaledMouseY <= contentY + 92) draggingY = true;
                if (unscaledMouseY >= contentY + 115 && unscaledMouseY <= contentY + 127) draggingZ = true;
            } else if (selectedTab == 2 && unscaledMouseX >= contentX + 170 && unscaledMouseX <= contentX + 206) {
                if (unscaledMouseY >= contentY && unscaledMouseY <= contentY + 16) ApexConfig.disableHurtCam = !ApexConfig.disableHurtCam;
                if (unscaledMouseY >= contentY + 30 && unscaledMouseY <= contentY + 46) ApexConfig.hudEnabled = !ApexConfig.hudEnabled;
                if (unscaledMouseY >= contentY + 60 && unscaledMouseY <= contentY + 76) ApexConfig.showFps = !ApexConfig.showFps;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            draggingScale = draggingX = draggingY = draggingZ = false;
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean shouldPause() { return false; }
    }
}
