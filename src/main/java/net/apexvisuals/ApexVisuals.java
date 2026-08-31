package net.apexvisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ApexVisuals implements ClientModInitializer {
    public static KeyBinding openMenuKey;

    // Переключатели Pulse Visuals
    public static boolean hudEnabled = true;
    public static boolean customAnimations = true;

    @Override
    public void onInitializeClient() {
        // 1. Регистрация кнопки вызова меню (Правый Shift)
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.apexvisuals.open_menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "ApexVisuals (Pulse)"
        ));

        // 2. Открытие GUI при нажатии Right Shift
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    client.setScreen(new PulseMenuScreen());
                }
            }
        });

        // 3. Отрисовка плашки HUD в левом верхнем углу
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!hudEnabled) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options.hudHidden) return;

            // Чёрный фон и красная полоска слева
            drawContext.fill(10, 10, 140, 30, 0xCC000000);
            drawContext.fill(10, 10, 12, 30, 0xFFFF0033);

            // Текст водяного знака и FPS
            drawContext.drawText(client.textRenderer, "APEX VISUALS", 18, 15, 0xFFFF0033, true);
            int fps = client.getCurrentFps();
            drawContext.drawText(client.textRenderer, "FPS: §f" + fps, 95, 15, 0xAAAAAAAA, true);
        });
    }

    // ==========================================
    // Графический экран меню (Pulse ClickGUI)
    // ==========================================
    public static class PulseMenuScreen extends Screen {

        public PulseMenuScreen() {
            super(Text.literal("Apex Visuals GUI"));
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            // Переключатель HUD
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("HUD Overlay: " + (hudEnabled ? "§aВКЛ" : "§cВЫКЛ")),
                button -> {
                    hudEnabled = !hudEnabled;
                    button.setMessage(Text.literal("HUD Overlay: " + (hudEnabled ? "§aВКЛ" : "§cВЫКЛ")));
                }
            ).dimensions(centerX - 100, centerY - 40, 200, 20).build());

            // Переключатель Анимаций
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Item Animations: " + (customAnimations ? "§aВКЛ" : "§cВЫКЛ")),
                button -> {
                    customAnimations = !customAnimations;
                    button.setMessage(Text.literal("Item Animations: " + (customAnimations ? "§aВКЛ" : "§cВЫКЛ")));
                }
            ).dimensions(centerX - 100, centerY - 10, 200, 20).build());

            // Кнопка Закрыть
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Закрыть"),
                button -> this.close()
            ).dimensions(centerX - 100, centerY + 30, 200, 20).build());
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);

            int centerX = this.width / 2;
            int centerY = this.height / 2;

            // Красно-чёрное окно меню Pulse
            context.fill(centerX - 120, centerY - 80, centerX + 120, centerY + 80, 0xE6111111);
            context.fill(centerX - 120, centerY - 80, centerX + 120, centerY - 77, 0xFFFF0033);

            // Заголовок
            context.drawCenteredTextWithShadow(this.textRenderer, "§c§lAPEX §fVISUALS §7(Pulse Client)", centerX, centerY - 68, 0xFFFFFFFF);

            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean shouldPause() {
            return false;
        }
    }
}
