package net.apexvisuals;

import net.apexvisuals.hud.ApexHudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class ApexVisuals implements ClientModInitializer {
    @Override
    
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(new ApexHudOverlay());
        System.out.println("[ApexVisuals] Visual mod initialized with Red & Black theme.");
    }
}
