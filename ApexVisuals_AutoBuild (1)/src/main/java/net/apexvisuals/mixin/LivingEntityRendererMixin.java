package net.apexvisuals.mixin;

import net.apexvisuals.config.ColorPalette;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "getOverlay", at = @At("HEAD"), cancellable = true)
    private static void customHitColor(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(ColorPalette.getHitColor());
    }
}
