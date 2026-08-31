package net.apexvisuals;

import net.apexvisuals.gui.PulseMenuScreen;
import net.apexvisuals.hud.ApexHudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ApexVisuals implements ClientModInitializer {
    public static KeyBinding openMenuKey;
    
    // Переключатели модулей (Pulse Visuals)
    public static boolean hudEnabled = true;
    public static boolean customAnimations = true;
    public static boolean redTheme = true;

    @Override
    public void onInitializeClient() {
        System.out.println("[ApexVisuals] Loaded Pulse Visuals System!");

        // Регистрация кнопки вызова меню (Правый Shift)
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.apexvisuals.open_menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "ApexVisuals (Pulse)"
        ));

        // Открытие GUI при нажатии Right Shift
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    client.setScreen(new PulseMenuScreen());
                }
            }
        });

        // Отрисовка HUD плашки на экране
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            ApexHudOverlay.render(drawContext);
        });
    }
}
