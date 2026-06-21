package me.kirill.my_first_mod.mixin;

import me.kirill.my_first_mod.item.Glauncher_v2;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Deprecated
@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    // Добираемся до внутренних переменных Майнкрафта, которые хранят
    // прогресс опускания/поднимания пушки для левой и правой руки
    @Shadow private float equipProgressMainHand;
    @Shadow private float prevEquipProgressMainHand;

    @Inject(
            method = "renderItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD")
    )
    private void lockEquipProgressForWeapon(
            AbstractClientPlayerEntity player, float tickDelta, float pitch,
            Hand hand, float swingProgress, ItemStack itemStack,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            CallbackInfo ci
    ) {
        // Если игрок держит в руках наш гранатомет
        if (itemStack.getItem() instanceof Glauncher_v2) {
            // МЫ НАМЕРТВО БЛОКИРУЕМ прогресс экипировки в 1.0 (предмет полностью поднят).
            // Теперь Майнкрафт физически не сможет опустить или дернуть пушку вниз при выстреле/КД!
            if (hand == Hand.MAIN_HAND) {
                this.equipProgressMainHand = 1.0F;
                this.prevEquipProgressMainHand = 1.0F;
            }
        }
    }
}