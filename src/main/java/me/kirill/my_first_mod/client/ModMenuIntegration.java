package me.kirill.my_first_mod.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.kirill.my_first_mod.My_first_mod;
import me.kirill.my_first_mod.cfg.Glauncher_v2_CFG;
import me.kirill.my_first_mod.networking.ModPackets;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder; // Проверь этот импорт!
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuIntegration::createConfigScreen;
    }

    public static Screen createConfigScreen(Screen parent) {

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Настройки Базуки"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        final float[] localPower = { My_first_mod.config.explosionPower };
        final int[] localDelay = { My_first_mod.config.fuseDelayTicks };
        final float[] localVelocity = { My_first_mod.config.shootVelocity };
        final float[] localVolume = { My_first_mod.config.soundVolume };

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("Общие"));

        general.addEntry(entryBuilder.startDoubleField(Text.literal("Сила взрыва"), localPower[0])
                .setDefaultValue(15.0)
                .setMin(0.0)
                .setMax(50.0)
                .setSaveConsumer(newValue -> localPower[0] = newValue.floatValue())
                .build());

        general.addEntry(entryBuilder.startIntField(Text.literal("Задержка взрывателя"), localDelay[0])
                .setDefaultValue(0)
                .setMin(0)
                .setSaveConsumer(newValue -> localDelay[0] = newValue)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Text.literal("Скорость выстрела"), localVelocity[0])
                .setDefaultValue(2.0)
                .setMin(0.5)
                .setMax(100.0)
                .setSaveConsumer(newValue -> localVelocity[0] = newValue.floatValue())
                .build());

        general.addEntry(entryBuilder.startDoubleField(Text.literal("Громкость выстрела"), localVolume[0])
                .setDefaultValue(1.0)
                .setMin(0.0)
                .setMax(2.0)
                .setSaveConsumer(newValue -> localVolume[0] = newValue.floatValue())
                .build());


        builder.setSavingRunnable(() -> {
            My_first_mod.config.explosionPower = localPower[0];
            My_first_mod.config.fuseDelayTicks = localDelay[0];
            My_first_mod.config.shootVelocity = localVelocity[0];
            My_first_mod.config.soundVolume = localVolume[0];

            AutoConfig.getConfigHolder(Glauncher_v2_CFG.class).save();

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeFloat(My_first_mod.config.explosionPower);
            buf.writeInt(My_first_mod.config.fuseDelayTicks);
            buf.writeFloat(My_first_mod.config.shootVelocity);
            buf.writeFloat(My_first_mod.config.soundVolume);

            ClientPlayNetworking.send(ModPackets.BAZOOKA_SETTINGS_SYNC, buf);
        });

        return builder.build();
    }
}