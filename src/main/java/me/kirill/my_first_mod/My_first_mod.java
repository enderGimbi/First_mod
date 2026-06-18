package me.kirill.my_first_mod;

import me.kirill.my_first_mod.cfg.Glauncher_v2_CFG;
import me.kirill.my_first_mod.networking.ModPackets;
import me.kirill.my_first_mod.render.GrenadeEntityRenderer;
import me.kirill.my_first_mod.util.IPlayerBazookaSettings;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class My_first_mod implements ModInitializer {

    public static Glauncher_v2_CFG config;

    @Override
    public void onInitialize() {
        // Регистрируем конфиг. AutoConfig сам найдет файл или создаст новый
        AutoConfig.register(Glauncher_v2_CFG.class, GsonConfigSerializer::new);

        // Загружаем данные из файла в нашу статическую переменную
        config = AutoConfig.getConfigHolder(Glauncher_v2_CFG.class).getConfig();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            // ВОТ ТУТ МЫ НАСТРАИВАЕМ ДЕФОЛТ ПРИ ВХОДЕ В ИГРУ
            if (player instanceof IPlayerBazookaSettings settings) {
                // Раньше тут, скорее всего, было Glauncher_v2_CFG.explosionPower
                // Теперь меняем на чтение из нашего сохраненного на диске файла:
                settings.setExplosionPower(My_first_mod.config.explosionPower);
                settings.setFuseDelay(My_first_mod.config.fuseDelayTicks);

                // И раз у тебя добавились скорость и громкость, их тоже берём из файла (если ты добавил их в Glauncher_v2_CFG):
                settings.setShootVelocity(My_first_mod.config.shootVelocity);
                settings.setSoundVolume(My_first_mod.config.soundVolume);
            }
        });
        ModItems.registerModItems();
        ModPotions.registerModPotions();
        ModPotions.registerPotionRecipes();
        ModBlocks.registerModBlocks();
        ModItemGroups.registerItemGroups();
        ModItems.registerAttackEvents();

        ModPackets.registerC2SPackets();
    }
}
