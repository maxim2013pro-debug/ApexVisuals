package net.apexvisuals;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Inject(
        method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
        at = @At("HEAD")
    )
    private void applyApexAnimations(
        LivingEntity entity,
        ItemStack item,
        ModelTransformationMode renderMode,
        boolean leftHanded,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        World world,
        int light,
        int overlay,
        int seed,
        CallbackInfo ci
    ) {
        if (entity == null) return;

        boolean isFirstPerson = renderMode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND || 
                                renderMode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND;

        if (isFirstPerson) {
            matrices.push();

            if (ApexVisuals.ApexConfig.customTransformEnabled) {
                matrices.scale(ApexVisuals.ApexConfig.itemScale, ApexVisuals.ApexConfig.itemScale, ApexVisuals.ApexConfig.itemScale);
                matrices.translate(ApexVisuals.ApexConfig.mainHandX, ApexVisuals.ApexConfig.mainHandY, ApexVisuals.ApexConfig.mainHandZ);
            }

            if (ApexVisuals.ApexConfig.oldBlockAnimation && item.getItem() instanceof SwordItem && entity.isUsingItem()) {
                matrices.translate(-0.15F, 0.15F, 0.0F);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-25.0F));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-45.0F));
            }
        }
    }

    @Inject(
        method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
        at = @At("RETURN")
    )
    private void popMatrix(LivingEntity entity, ItemStack item, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int light, int overlay, int seed, CallbackInfo ci) {
        boolean isFirstPerson = renderMode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND || 
                                renderMode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND;
        if (isFirstPerson && entity != null) {
            matrices.pop();
        }
    }
}
