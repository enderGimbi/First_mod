package me.kirill.my_first_mod.item;

import me.kirill.my_first_mod.ModEntities;
import me.kirill.my_first_mod.My_first_mod;
import me.kirill.my_first_mod.entity.GrenadeEntity;
import me.kirill.my_first_mod.util.IPlayerBazookaSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class Glauncher_v2 extends GLauncher {

    float divergence = 0.1f; // разброс

    public Glauncher_v2(Settings settings) {
        super(settings);
    }

    @Override
    public void shot(World world, PlayerEntity player) {
        // Объединяем проверки: строго сервер + проверяем наш интерфейс настроек
        if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer && serverPlayer instanceof IPlayerBazookaSettings settings) {

            float calcPitch = 1.0f - (settings.getExplosionPower() / 50.0f);
            float pitch = Math.max(0.3f, calcPitch) + (world.random.nextFloat() * 0.1f - 0.05f);

            // Создаем гранату, передавая ей ИНДИВИДУАЛЬНЫЕ настройки этого игрока
            GrenadeEntity grenade = new GrenadeEntity(
                    ModEntities.GRENADE_TYPE, world, serverPlayer,
                    settings.getExplosionPower(),
                    settings.getFuseDelay(),
                    settings.getShootVelocity(),
                    settings.getSoundVolume()
            );

            grenade.setOwner(serverPlayer);

            // ИСПРАВЛЕНО: Теперь берем скорость ИЗ НАСТРОЕК ИГРОКА (settings), а не из My_first_mod.config!
            grenade.setVelocity(serverPlayer, serverPlayer.getPitch(), serverPlayer.getYaw(), 0.0f, settings.getShootVelocity(), divergence);

            // Звук выстрела
            world.playSound(
                    null,
                    serverPlayer.getBlockPos(),
                    SoundEvents.ENTITY_WITHER_SHOOT,
                    SoundCategory.PLAYERS,
                    settings.getSoundVolume(),
                    pitch
            );

            world.spawnEntity(grenade);
        }
    }
}