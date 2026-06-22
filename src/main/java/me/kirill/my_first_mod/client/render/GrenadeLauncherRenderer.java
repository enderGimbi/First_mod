package me.kirill.my_first_mod.client.render;

import me.kirill.my_first_mod.client.model.GrenadeLauncherModel;
import me.kirill.my_first_mod.item.Glauncher_v2;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class GrenadeLauncherRenderer extends GeoItemRenderer<Glauncher_v2> {

    public GrenadeLauncherRenderer() {
        super(new GrenadeLauncherModel());
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode transformType, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        this.animatable = (Glauncher_v2) stack.getItem();
        this.currentItemStack = stack;

        // Вызываем супер-метод, чтобы GeckoLib запек и подготовил модель к этому кадру
        super.render(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
    }
}