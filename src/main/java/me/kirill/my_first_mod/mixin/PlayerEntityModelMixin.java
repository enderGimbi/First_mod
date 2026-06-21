package me.kirill.my_first_mod.mixin;

import me.kirill.my_first_mod.item.Glauncher_v2;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin {

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void setCustomWeaponAngles(LivingEntity livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        PlayerEntityModel<?> model = (PlayerEntityModel<?>)(Object)this;

        boolean hasInMainHand = livingEntity.getStackInHand(Hand.MAIN_HAND).getItem() instanceof Glauncher_v2;
        boolean hasInOffHand = livingEntity.getStackInHand(Hand.OFF_HAND).getItem() instanceof Glauncher_v2;

        if (hasInMainHand || hasInOffHand) {
            // Вычисляем горизонтальную дельту (разницу между поворотом головы и тела)
            // Это позволит руке мгновенно поворачиваться влево/вправо вслед за прицелом
            float yawDelta = model.head.yaw - model.body.yaw;

            // Торс плавно следует за головой
            model.body.pitch = model.head.pitch * 0.15F;
            model.body.yaw = model.head.yaw * 0.25F; // Чуть увеличили отзывчивость тела

            model.jacket.pitch = model.body.pitch;
            model.jacket.yaw = model.body.yaw;

            // Логика для ПРАВОЙ руки
            if (hasInMainHand) {
                // Вертикальное прицеливание (вверх/вниз)
                model.rightArm.pitch = (float) Math.toRadians(-90.0) + model.head.pitch;
                model.rightArm.yaw = yawDelta;
                model.rightArm.roll = 0.0F;

                // Синхронизация рукава
                model.rightSleeve.pitch = model.rightArm.pitch;
                model.rightSleeve.yaw = model.rightArm.yaw;
                model.rightSleeve.roll = model.rightArm.roll;
            }

            // Логика для ЛЕВОЙ руки (зеркальная)
            if (hasInOffHand) {
                model.leftArm.pitch = (float) Math.toRadians(-90.0) + model.head.pitch;
                model.leftArm.yaw = yawDelta;
                model.leftArm.roll = 0.0F;

                // Синхронизация рукава
                model.leftSleeve.pitch = model.leftArm.pitch;
                model.leftSleeve.yaw = model.leftArm.yaw;
                model.leftSleeve.roll = model.leftArm.roll;
            }
        }
    }
}