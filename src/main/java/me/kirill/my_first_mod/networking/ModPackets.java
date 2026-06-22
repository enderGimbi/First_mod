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

        ServerPlayNetworking.registerGlobalReceiver(GLAUNCHER_V2_GRENADE_SPAWN, (server, player, handler, buf, responseSender) -> {
            // Читаем строго в порядке записи FIFO!
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            double particleX = buf.readDouble();
            double particleY = buf.readDouble();
            double particleZ = buf.readDouble();
            int count = buf.readInt();

            System.out.println("Полученные координаты нормализованного спавна гранаты: ("+x+", "+y+", "+z+")");
            System.out.println("Полученные координаты нормализованного партикла: ("+particleX+", "+particleY+", "+particleZ+")");

            server.execute(() -> {
                if (player instanceof IPlayerBazookaSettings settings) {
                    ServerWorld serverWorld = player.getServerWorld();
                    Vec3d muzzlePos = new Vec3d(x, y, z);

                    // 1. Спавним саму гранату
                    GrenadeEntity grenade = new GrenadeEntity(
                            ModEntities.GRENADE_TYPE, serverWorld, player,
                            settings.getExplosionPower(),
                            settings.getFuseDelay(),
                            settings.getShootVelocity(),
                            settings.getSoundVolume()
                    );
                    grenade.setOwner(player);
                    grenade.setPosition(muzzlePos);
                    grenade.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, settings.getShootVelocity(), 0.1f);
                    serverWorld.spawnEntity(grenade);

                    // 2. Звук выстрела
                    float calcPitch = 1.0f - (settings.getExplosionPower() / 50.0f);
                    float pitch = Math.max(0.3f, calcPitch) + (serverWorld.random.nextFloat() * 0.1f - 0.05f);
                    serverWorld.playSound(null, muzzlePos.x, muzzlePos.y, muzzlePos.z, SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, settings.getSoundVolume(), pitch);

                    // 3. Базовые эффекты на дуле
                    //serverWorld.spawnParticles(ParticleTypes.EXPLOSION, muzzlePos.x, muzzlePos.y, muzzlePos.z, 1, 0, 0, 0, 0);
                    //serverWorld.spawnParticles(ParticleTypes.FLAME, muzzlePos.x, muzzlePos.y, muzzlePos.z, 5, 0.05, 0.05, 0.05, 0.1);

                    // 4. НАПРАВЛЕННЫЙ СПАВН ПАРТИКЛОВ НАЗАД (ПРОТИВОВЕС ВЗГЛЯДУ)
                    // Получаем вектор взгляда игрока (для Yarn: getRotationVector, для Mojang: getLookAngle)
                    Vec3d lookVec = player.getRotationVector();

                    // Задаем силу скорости отлета назад (например, 0.5)
                    double backSpeed = 0.5;

                    // Инвертируем вектор (разворачиваем назад) и умножаем на скорость
                    double velocityX = -lookVec.x * backSpeed;
                    double velocityY = -lookVec.y * backSpeed;
                    double velocityZ = -lookVec.z * backSpeed;

                    // Так как нам нужна индивидуальная скорость для каждого партикла, мы используем цикл,
                    // передавая '0' в количество, чтобы активировать режим Velocity.
                    for (int i = 0; i < count; i++) {
                        // Небольшой случайный разброс, чтобы партиклы летели не одной тонкой линией,
                        // а реалистичным конусом назад
                        double spreadX = (serverWorld.random.nextDouble() - 0.5) * 0.5;
                        double spreadY = (serverWorld.random.nextDouble() - 0.5) * 0.5;
                        double spreadZ = (serverWorld.random.nextDouble() - 0.5) * 0.5;

                        serverWorld.spawnParticles(
                                ParticleTypes.FLAME,
                                particleX, particleY, particleZ, // Спавним на вашем локаторе for_fire
                                0,                               // СТРОГО 0, чтобы аргументы ниже стали скоростью!
                                velocityX + spreadX,             // Скорость по X (направлена назад + разброс)
                                velocityY + spreadY,             // Скорость по Y (направлена назад + разброс)
                                velocityZ + spreadZ,             // Скорость по Z (направлена назад + разброс)
                                1.0                              // Множитель скорости (оставляем 1.0, так как скорость уже в векторе)
                        );
                    }
                }
            });
        });

    }
}