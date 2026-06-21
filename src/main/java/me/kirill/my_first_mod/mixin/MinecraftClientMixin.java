package me.kirill.my_first_mod.mixin;

import me.kirill.my_first_mod.item.Glauncher_v2;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void cancelSwingForWeapon(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient)(Object)this;
        ClientPlayerEntity player = client.player;

        if (player != null) {
            // Проверяем, что игрок держит наш гранатомёт в активной руке
            if (player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof Glauncher_v2) {
                // Хитрый трюк: мы принудительно говорим игре, что рука уже совершила взмах,
                // поэтому Майнкрафт не будет запускать ванильное дёрганье визуально!
                player.swingHand(Hand.MAIN_HAND, false);
            }
        }
    }
}