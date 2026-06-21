package me.kirill.my_first_mod.networking;

import me.kirill.my_first_mod.ModEntities;
import me.kirill.my_first_mod.entity.GrenadeEntity;
import me.kirill.my_first_mod.util.IPlayerBazookaSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.WGLARBRobustnessApplicationIsolation;

public class ModPackets {
    // Идентификаторы сетевого пакета
    public static final Identifier GLAUNCHER_V2_SETTINGS_SYNC = new Identifier("my_first_mod", "bazooka_sync");
    public static final Identifier GLAUNCHER_V2_GRENADE_SPAWN = new Identifier("my_first_mod","grenade_spawn");


    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(GLAUNCHER_V2_SETTINGS_SYNC, (server, player, handler, buf, responseSender) -> {
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
        ServerPlayNetworking.registerGlobalReceiver(GLAUNCHER_V2_GRENADE_SPAWN,(server, player, handler, buf, responseSender) -> {
            // Обязательно в таком порядке!!
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();


            server.execute(() -> {
                if (player instanceof IPlayerBazookaSettings settings){

                    ServerWorld serverWorld = player.getServerWorld();

                    Vec3d muzzlePos = new Vec3d(x,y,z);
                    GrenadeEntity grenade = new GrenadeEntity(
                            ModEntities.GRENADE_TYPE, serverWorld,player,
                            settings.getExplosionPower(),
                            settings.getFuseDelay(),
                            settings.getShootVelocity(),
                            settings.getSoundVolume()
                    );
                    grenade.setOwner(player);
                    grenade.setPosition(muzzlePos);
                    grenade.setVelocity(player,player.getPitch(),player.getYaw(),0.0f,settings.getShootVelocity(),0.1f);
                    serverWorld.spawnEntity(grenade);

                    float calcPitch = 1.0f - (settings.getExplosionPower() / 50.0f);
                    float pitch = Math.max(0.3f, calcPitch) + (serverWorld.random.nextFloat() * 0.1f - 0.05f);
                    serverWorld.playSound(null, muzzlePos.x,muzzlePos.y,muzzlePos.z, SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, settings.getSoundVolume(), pitch);

                    serverWorld.spawnParticles(ParticleTypes.EXPLOSION, muzzlePos.x, muzzlePos.y, muzzlePos.z, 1, 0, 0, 0, 0);
                    serverWorld.spawnParticles(ParticleTypes.FLAME, muzzlePos.x, muzzlePos.y, muzzlePos.z, 5, 0.05, 0.05, 0.05, 0.1);
                }
            });
        });
    }
}