package me.kirill.my_first_mod.client.render;

import me.kirill.my_first_mod.client.model.GrenadeModel;
import me.kirill.my_first_mod.entity.GrenadeEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GrenadeRenderer extends GeoEntityRenderer<GrenadeEntity> {

    public GrenadeRenderer(EntityRendererFactory.Context renderManager) {
        // Передаем менеджер рендеринга и нашу кастомную модель GrenadeModel
        super(renderManager, new GrenadeModel());
    }

    @Override
    public void preRender(MatrixStack poseStack, GrenadeEntity animatable, BakedGeoModel model, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        float yaw = animatable.getYaw(partialTick);
        float pitch = animatable.getPitch(partialTick);

        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-pitch));
    }
}