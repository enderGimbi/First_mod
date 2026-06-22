package me.kirill.my_first_mod.entity;

import me.kirill.my_first_mod.ModEntities;
import me.kirill.my_first_mod.My_first_mod;
import me.kirill.my_first_mod.util.IPlayerBazookaSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;

public class GrenadeEntity extends ThrownItemEntity implements GeoEntity {

    public float explosionPower;
    public int fuseTicks;
    public float shootVelocity;
    public float soundVolume;

    private static final TrackedData<Boolean> HAS_COLLIDED = DataTracker.registerData(GrenadeEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> EXPLOSION_POWER = DataTracker.registerData(GrenadeEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> FUSE_TICKS = DataTracker.registerData(GrenadeEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private UUID ownerUuid;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("first_animation");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GeoAnimatable>(this,"controller",0,event ->{
            return event.setAndContinue(FLY_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // Главный конструктор, который Майнкрафт вызывает автоматически (при спавне, чтении из мира и т.д.)
    public GrenadeEntity(EntityType<GrenadeEntity> entityType, World world) {
        super(entityType, world);
        // Загружаем значения по умолчанию из конфига в обычные переменные
        this.explosionPower = My_first_mod.config.explosionPower;
        this.fuseTicks = My_first_mod.config.fuseDelayTicks;
        this.shootVelocity = My_first_mod.config.shootVelocity;
        this.soundVolume = My_first_mod.config.soundVolume;
    }

    @SuppressWarnings("ConstantValue")
    public GrenadeEntity(World world, LivingEntity owner) {
        super(ModEntities.GRENADE_TYPE, owner, world);
        if (owner != null) {
            this.ownerUuid = owner.getUuid();

            if (owner instanceof IPlayerBazookaSettings settings) {
                // Обновляем локальные переменные
                this.explosionPower = settings.getExplosionPower();
                this.fuseTicks = settings.getFuseDelay();
                this.shootVelocity = settings.getShootVelocity();
                this.soundVolume = settings.getSoundVolume();

                // ЖЕЛЕЗНО обновляем сетевой DataTracker, чтобы метод взрыва увидел изменения!
                this.setExplosionPower(settings.getExplosionPower());
                this.setFuseTicks(settings.getFuseDelay());
            }
        }
    }

    @SuppressWarnings("ConstantValue")
    public GrenadeEntity(EntityType<? extends ThrownItemEntity> entityType, LivingEntity livingEntity, World world) {
        super(entityType, livingEntity, world);
        if (livingEntity != null) {
            this.ownerUuid = livingEntity.getUuid();

            if (livingEntity instanceof IPlayerBazookaSettings settings) {
                this.explosionPower = settings.getExplosionPower();
                this.fuseTicks = settings.getFuseDelay();
                this.shootVelocity = settings.getShootVelocity();
                this.soundVolume = settings.getSoundVolume();

                // Точно так же пушим данные в трекер
                this.setExplosionPower(settings.getExplosionPower());
                this.setFuseTicks(settings.getFuseDelay());
            }
        }
    }

    @SuppressWarnings("ConstantValue")
    public GrenadeEntity(EntityType<? extends ThrownItemEntity> type, World world, LivingEntity owner,
                         float power, int delay, float velocity, float volume) {
        super(type, owner, world);
        if (owner != null) {
            this.ownerUuid = owner.getUuid();
        }
        this.explosionPower = power;
        this.fuseTicks = delay;
        this.shootVelocity = velocity;
        this.soundVolume = volume;

        // Передаем кастомные значения в сетевой трекер
        this.setExplosionPower(power);
        this.setFuseTicks(delay);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.TNT;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        if (!this.getWorld().isClient) {
            if (!this.isHasCollided()) {
                this.setVelocity(0.0, 0.0, 0.0);
                this.setHasCollided(true);
            }
        }
    }

    @Override
    public void tick() {
        if (this.isHasCollided()) {
            this.setVelocity(0.0, 0.0, 0.0);
        } else {
            // ЛОГИКА СЕРВЕРА (Коллизии)
            if (!this.getWorld().isClient) {
                Vec3d actualVelocity = this.getVelocity();
                Vec3d lookVector = this.getRotationVector().normalize();
                Vec3d checkVelocity = actualVelocity.add(lookVector);

                this.setVelocity(checkVelocity);
                HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
                this.setVelocity(actualVelocity);

                if (hitResult.getType() != HitResult.Type.MISS) {
                    this.onCollision(hitResult);
                }
            }
            // ЛОГИКА КЛИЕНТА (Трассер из частиц)
            else {
                // Спавним частицы только тогда, когда граната реально летит (скорость не нулевая)
                if (this.getVelocity().lengthSquared() > 0.001) {
                    // Центрируем частицу по центру хитбокса гранаты
                    double x = this.getX();
                    double y = this.getBodyY(0.5); // 0.5 — середина высоты сущности
                    double z = this.getZ();

                    // Спавним белый след. Каждые несколько под-тиков (или просто 1-2 частицы за тик)
                    // ParticleTypes.POOF — красивое белое облачко дыма
                    // Если хочешь тонкую светящуюся линию, замени POOF на END_ROD
                    this.getWorld().addParticle(
                            ParticleTypes.CLOUD,
                            x, y, z,
                            0.0, 0.0, 0.0 // Скорость самой частицы (0.0 означает, что она будет висеть на месте, образуя ровный след)
                    );
                }
            }
        }

        super.tick();

        if (!this.getWorld().isClient && this.isHasCollided()) {
            int currentTicks = getFuseTicks() - 1;
            setFuseTicks(currentTicks);
            if (currentTicks <= 0) {
                explosion(this.getWorld(), this);
                this.discard();
            }
        }
    }

//    public void explosion(World world, Entity entity) {
//        world.createExplosion(this, this.getX(), this.getY(), this.getZ(), explosionPower, World.ExplosionSourceType.TNT);
//    }

    public void explosion(World world, Entity grenade) {
        if (world.isClient) return; // Строго на сервере

        float maxPower = getExplosionPower();
        int radius = (int) Math.ceil(maxPower);
        float radiusSq = maxPower * maxPower;

        int centerX = (int) Math.floor(grenade.getX());
        int centerY = (int) Math.floor(grenade.getY());
        int centerZ = (int) Math.floor(grenade.getZ());

        // =========================================================================
        // 1. УРОН ПО МОБАМ (Динамический расчет от Силы Взрыва + Затухание к краям)
        // =========================================================================
        // Мин/Макс урон при минимальной/максимальной силе взрыва (балансируй эти цифры)
        float damageAtMinPower = 100.0f; // Урон в центре, если сила взрыва минимальна
        float damageAtMaxPower = 500.0f; // Урон в центре, если сила взрыва максимальна

        // Границы самой силы взрыва для калибровки твоей формулы
        float minAllowedPower = 1.0f;
        float maxAllowedPower = 50.0f;

        // Вычисляем коэффициент силы взрыва конкретно для расчета урона (от 0.0 до 1.0)
        float damagePowerRatio = (maxPower - minAllowedPower) / (maxAllowedPower - minAllowedPower);
        damagePowerRatio = Math.max(0.0f, Math.min(1.0f, damagePowerRatio)); // Ограничиваем в пределах [0, 1]

        // Шаг 1: Находим урон в самом эпицентре конкретно для ТЕКУЩЕЙ силы взрыва
        float damageInEpicenter = damageAtMinPower + (damageAtMaxPower - damageAtMinPower) * damagePowerRatio;

        net.minecraft.util.math.Box damageBox = new net.minecraft.util.math.Box(
                grenade.getX() - radius, grenade.getY() - radius, grenade.getZ() - radius,
                grenade.getX() + radius, grenade.getY() + radius, grenade.getZ() + radius
        );
        java.util.List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, damageBox, Entity::isAlive);

        LivingEntity grenadeOwner = null;
        if (this.ownerUuid != null && world instanceof ServerWorld serverWorld) {
            Entity foundEntity = serverWorld.getEntity(this.ownerUuid);
            if (foundEntity instanceof LivingEntity) {
                grenadeOwner = (LivingEntity) foundEntity;
            }
        }
        DamageSource damageSource = world.getDamageSources().explosion(grenade, grenadeOwner);

        for (LivingEntity target : targets) {
            double distance = target.distanceTo(grenade);

            if (distance <= radius) {
                // Шаг 2: Считаем падение урона от центра к краям взрывной волны
                float distanceMultiplier = 1.0f - (float)(distance / radius);
                distanceMultiplier = Math.max(0.0f, Math.min(1.0f, distanceMultiplier));

                // Итоговый урон: базовый урон эпицентра умножаем на близость моба к центру
                float finalDamage = damageInEpicenter * distanceMultiplier;

                // Небольшая защита: если моба задело самым краем, нанесем хотя бы 1 единицу урона
                if (finalDamage < 1.0f && distanceMultiplier > 0.05f) {
                    finalDamage = 1.0f;
                }

                target.damage(damageSource, finalDamage);

                // Твоя физика отдачи
                double dirX = target.getX() - grenade.getX();
                double dirY = target.getY() - grenade.getY();
                double dirZ = target.getZ() - grenade.getZ();
                double len = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
                if (len > 0) {
                    target.addVelocity(
                            (dirX / len) * distanceMultiplier * 1.5,
                            (dirY / len) * distanceMultiplier * 1.2 + 0.2,
                            (dirZ / len) * distanceMultiplier * 1.5
                    );
                    target.velocityModified = true;
                }
            }
        }

        // =========================================================================
        // 2. ФАЗА 1: БЫСТРЫЙ СБОР КООРДИНАТ СФЕРЫ В ПАМЯТЬ
        // =========================================================================
        List<BlockPos> TargetPositions = new ArrayList<>();

        for (int x = 0; x <= radius; x++) {
            int xSq = x * x;
            for (int y = 0; y <= radius; y++) {
                int xSqPlusYSq = xSq + (y * y);
                for (int z = 0; z <= radius; z++) {
                    int currentDistanceSq = xSqPlusYSq + (z * z);

                    if (currentDistanceSq <= radiusSq) {
                        for (int sx : x == 0 ? new int[]{0} : new int[]{x, -x}) {
                            for (int sy : y == 0 ? new int[]{0} : new int[]{y, -y}) {
                                for (int sz : z == 0 ? new int[]{0} : new int[]{z, -z}) {
                                    TargetPositions.add(new BlockPos(centerX + sx, centerY + sy, centerZ + sz));
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 3. ФАЗА 2: ВЕКТОРНЫЙ ПРОСЧЕТ ЛУЧЕЙ (С твоей кастомной функцией затухания)
        // =========================================================================
        Set<BlockPos> blocksToDestroy = new HashSet<>();

        for (BlockPos targetPos : TargetPositions) {
            double dx = targetPos.getX() - centerX;
            double dy = targetPos.getY() - centerY;
            double dz = targetPos.getZ() - centerZ;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance == 0) continue;

            double stepX = dx / distance;
            double stepY = dy / distance;
            double stepZ = dz / distance;

            // Сила луча в центре взрыва
            float beamPower = maxPower * (0.7f + world.random.nextFloat() * 0.6f);

            for (double d = 0; d <= distance; d += 0.5) {
                int checkX = (int) Math.floor(centerX + stepX * d);
                int checkY = (int) Math.floor(centerY + stepY * d);
                int checkZ = (int) Math.floor(centerZ + stepZ * d);

                BlockPos currentPos = new BlockPos(checkX, checkY, checkZ);
                BlockState blockState = world.getBlockState(currentPos);

                if (!blockState.isAir()) {
                    float resistance = blockState.getBlock().getBlastResistance();

                    if (resistance >= 1200.0f) {
                        break; // Бедрок поглощает луч полностью
                    }

                    float resistanceCost = (resistance + 0.3f) * 0.3f;
                    beamPower -= resistanceCost;

                    // Проверяем: жива ли еще сила луча И хватает ли текущей расчетной мощности (из твоей функции),
                    // чтобы преодолеть сопротивление конкретного блока на этом расстоянии d
                    if (beamPower >= 0 && currentPowerCalculated(maxPower, d, radius) >= resistance) {
                        blocksToDestroy.add(currentPos.toImmutable());
                    } else {
                        break; // Луч застрял в блоке
                    }
                }

                beamPower -= 0.1f;
                if (beamPower <= 0) break;
            }
        }

        // =========================================================================
        // 4. ФАЗА 3: ИСПРАВЛЕННОЕ УНИЧТОЖЕНИЕ БЛОКОВ (Без лагов, дропа семян и звуков)
        // =========================================================================
        for (BlockPos pos : blocksToDestroy) {
            if (world.getBlockState(pos).getBlock().getBlastResistance() < 1200.0f) {
                // Флаги 2 | 16 стирают блок в воздухе, не вызывая триггеры разрушения,
                // которые заставляли ломаться и выпадать траву, семена и издавать звуки!
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2 | 16);
            }
        }


        float minPowerRange = 5.0f;
        float maxPowerRange = 50.0f;
        float powerRatio = (maxPower-minPowerRange)/(maxPowerRange-minPowerRange);
        powerRatio = Math.max(0.0f,Math.min(1.0f,powerRatio));
        float dynamicPitch = 1.0f - (powerRatio*0.5f);

        world.playSound(null,
                grenade.getBlockPos(),
                SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.PLAYERS,
                Math.max(2.0f,2.0f*(explosionPower/3.0f)*soundVolume),
                dynamicPitch);



        // =========================================================================
        // 5. ФАЗА 4: СПАВН ЧАСТИЦ (Ванильные + Кастомные)
        // =========================================================================
        if (world instanceof ServerWorld serverWorld) {
            double pX = grenade.getX();
            double pY = grenade.getY() + 0.1;
            double pZ = grenade.getZ();

            // --- ВАНИЛЬНЫЕ ЧАСТИЦЫ ---
            // 1. Огромное облако взрыва (главный визуальный бабах)
            serverWorld.spawnParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    pX, pY, pZ,
                    1,          // Количество (для EMITTER достаточно 1, она сама по себе большая)
                    0.0, 0.0, 0.0, // Смещение по X, Y, Z
                    0.0         // Скорость частиц
            );

            // 2. Огненные искры, разлетающиеся из центра взрыва
            serverWorld.spawnParticles(
                    ParticleTypes.FLAME,
                    pX, pY, pZ,
                    (int)(explosionPower*3),         // Спавним 20 штук
                    radius * 0.5, radius * 0.5, radius * 0.5, // Разброс в пределах половины радиуса взрыва
                    0.2         // Скорость разлета (0.2 заставит их красиво разлететься в стороны)
            );

            // 3. Густой серый дым, поднимающийся вверх
            serverWorld.spawnParticles(
                    ParticleTypes.LARGE_SMOKE,
                    pX, pY, pZ,
                    (int)(explosionPower*10),         // Спавним 35 частиц дыма
                    radius * 0.6, radius * 0.6, radius * 0.6, // Разброс дыма по воронке
                    0.05        // Небольшая скорость, чтобы дым лениво клубился
            );


            // --- ТВОИ КАСТОМНЫЕ ЧАСТИЦЫ ---
            // Когда ты зарегистрируешь свою кастомную частицу (например, MY_GRENADE_PARTICLE),
            // ты сможешь спавнить её точно так же.
            // Раскомментируй и замени ModParticles.MY_GRENADE_PARTICLE на свой класс/поле:
        /*
        serverWorld.spawnParticles(
                ModParticles.MY_GRENADE_PARTICLE,
                pX, pY, pZ,
                15,         // Количество твоих частиц
                0.3, 0.3, 0.3, // Минимальное смещение вокруг гранаты
                0.1         // Скорость
        );
        */
        }
    }

    private float currentPowerCalculated(float maxPower, double currentDist, int radius) {
        return maxPower * (1.0f - (float)(currentDist / radius));
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    protected boolean canHit(Entity entity) {
        if (java.util.Objects.equals(this.ownerUuid,entity.getUuid())) {
            return false;
        }
        if (entity == this.getOwner()) {
            return false;
        }
        return super.canHit(entity);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(HAS_COLLIDED, false);
        this.dataTracker.startTracking(EXPLOSION_POWER, My_first_mod.config.explosionPower);
        this.dataTracker.startTracking(FUSE_TICKS, My_first_mod.config.fuseDelayTicks);
    }

    public boolean isHasCollided() {
        return this.dataTracker.get(HAS_COLLIDED);
    }

    public void setHasCollided(boolean collided) {
        this.dataTracker.set(HAS_COLLIDED, collided);
    }

    public float getExplosionPower() {
        return this.dataTracker.get(EXPLOSION_POWER);
    }

    public void setExplosionPower(float power) {
        this.dataTracker.set(EXPLOSION_POWER, power);
    }

    public int getFuseTicks() {
        return this.dataTracker.get(FUSE_TICKS);
    }

    public void setFuseTicks(int ticks) {
        this.dataTracker.set(FUSE_TICKS, ticks);
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose){
        return EntityDimensions.changing(0.375f,0.375f);
    }
}