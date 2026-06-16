package me.kirill.my_first_mod.networking;

import me.kirill.my_first_mod.util.IPlayerBazookaSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class ModPackets {
    // Используй id своего мода вместо "my_first_mod", если он другой
    public static final Identifier BAZOOKA_SETTINGS_SYNC = new Identifier("my_first_mod", "bazooka_sync");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(BAZOOKA_SETTINGS_SYNC, (server, player, handler, buf, responseSender) -> {
            // Читаем ровно в том порядке, в каком клиент будет записывать!
            float power = buf.readFloat();
            int delay = buf.readInt();
            float velocity = buf.readFloat();
            float volume = buf.readFloat();

            server.execute(() -> {
                if (player instanceof IPlayerBazookaSettings settings) {
                    settings.setExplosionPower(power);
                    settings.setFuseDelay(delay);
                    settings.setShootVelocity(velocity);
                    settings.setSoundVolume(volume);
                }
            });
        });
    }
}