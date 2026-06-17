package me.kirill.my_first_mod.entity;

import me.kirill.my_first_mod.ModEntities;
import me.kirill.my_first_mod.My_first_mod;
import me.kirill.my_first_mod.util.IPlayerBazookaSettings;
import net.minecraft.entity.*;
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
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import java.util.UUID;

public class GrenadeEntity extends ThrownItemEntity {

    public float explosionPower;
    public int fuseTicks;
    private static final TrackedData<Boolean> HAS_COLLIDED = DataTracker.registerData(GrenadeEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> EXPLOSION_POWER = DataTracker.registerData(GrenadeEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> FUSE_TICKS = DataTracker.registerData(GrenadeEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private UUID ownerUuid;

    public float shootVelocity;
    public float soundVolume;

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
            if (!this.getWorld().isClient) {
                HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
                if (hitResult.getType() != HitResult.Type.MISS) {
                    this.onCollision(hitResult);
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
        // 1. УРОН ПО МОБАМ (Остается O(N) через быстрый хитбокс)
        // =========================================================================
        net.minecraft.util.math.Box damageBox = new net.minecraft.util.math.Box(
                grenade.getX() - radius, grenade.getY() - radius, grenade.getZ() - radius,
                grenade.getX() + radius, grenade.getY() + radius, grenade.getZ() + radius
        );
        java.util.List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, damageBox, Entity::isAlive);

        net.minecraft.entity.LivingEntity grenadeOwner = null;
        if (this.ownerUuid != null && world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            Entity foundEntity = serverWorld.getEntity(this.ownerUuid);
            if (foundEntity instanceof net.minecraft.entity.LivingEntity) {
                grenadeOwner = (net.minecraft.entity.LivingEntity) foundEntity;
            }
        }
        net.minecraft.entity.damage.DamageSource damageSource = world.getDamageSources().explosion(grenade, grenadeOwner);

        for (LivingEntity target : targets) {
            double distance = target.distanceTo(grenade);
            if (distance <= radius) {
                float damageMultiplier = 1.0f - (float)(distance / radius);
                target.damage(damageSource, maxPower * 2.0f * damageMultiplier);

                double dirX = target.getX() - grenade.getX();
                double dirY = target.getY() - grenade.getY();
                double dirZ = target.getZ() - grenade.getZ();
                double len = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
                if (len > 0) {
                    target.addVelocity((dirX / len) * damageMultiplier * 1.5, (dirY / len) * damageMultiplier * 1.2 + 0.2, (dirZ / len) * damageMultiplier * 1.5);
                    target.velocityModified = true;
                }
            }
        }

        // =========================================================================
        // 2. ФАЗА 1: БЫСТРЫЙ СБОР КООРДИНАТ СФЕРЫ В ПАМЯТЬ (Оптимизация в 8 раз!)
        // =========================================================================
        // Используем структуру данных для хранения предрассчитанных относительных координат
        java.util.List<net.minecraft.util.math.BlockPos> TargetPositions = new java.util.ArrayList<>();

        // Перебираем только ОДНУ ЧЕТВЕРТЬ куба (только положительные x, y, z)
        for (int x = 0; x <= radius; x++) {
            int xSq = x * x;
            for (int y = 0; y <= radius; y++) {
                int xSqPlusYSq = xSq + (y * y);
                for (int z = 0; z <= radius; z++) {
                    int currentDistanceSq = xSqPlusYSq + (z * z);

                    // Если точка попала в сферу
                    if (currentDistanceSq <= radiusSq) {
                        // Генерируем зеркальные копии этой точки для всех 8 направлений (симметрия)
                        for (int sx : x == 0 ? new int[]{0} : new int[]{x, -x}) {
                            for (int sy : y == 0 ? new int[]{0} : new int[]{y, -y}) {
                                for (int sz : z == 0 ? new int[]{0} : new int[]{z, -z}) {
                                    TargetPositions.add(new net.minecraft.util.math.BlockPos(centerX + sx, centerY + sy, centerZ + sz));
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 3. ФАЗА 2: ВЕКТОРНЫЙ ПРОСЧЕТ ЛУЧЕЙ (Учет преград и экранирования)
        // =========================================================================
        // Перебираем только точки НА ПОВЕРХНОСТИ сферы, чтобы пустить к ним лучи.
        // Для этого отфильтруем наш список, оставив позиции, которые находятся на краю.

        java.util.Set<net.minecraft.util.math.BlockPos> blocksToDestroy = new java.util.HashSet<>();

        for (net.minecraft.util.math.BlockPos targetPos : TargetPositions) {
            // Пускаем луч из центра взрыва (grenade.getPos()) к этой точке
            double dx = targetPos.getX() - centerX;
            double dy = targetPos.getY() - centerY;
            double dz = targetPos.getZ() - centerZ;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance == 0) continue;

            // Нормализуем вектор (делаем шаг длиной в 1 блок или меньше)
            double stepX = dx / distance;
            double stepY = dy / distance;
            double stepZ = dz / distance;

            // Начальная сила луча в центре
            float beamPower = maxPower * (0.7f + world.random.nextFloat() * 0.6f); // Хаос на старте луча

            // Шагаем по лучу от центра к финальной точке
            for (double d = 0; d <= distance; d += 0.5) { // шаг 0.5 для точности, чтобы не пропустить углы блоков
                int checkX = (int) Math.floor(centerX + stepX * d);
                int checkY = (int) Math.floor(centerY + stepY * d);
                int checkZ = (int) Math.floor(centerZ + stepZ * d);

                net.minecraft.util.math.BlockPos currentPos = new net.minecraft.util.math.BlockPos(checkX, checkY, checkZ);
                net.minecraft.block.BlockState blockState = world.getBlockState(currentPos);

                if (!blockState.isAir()) {
                    float resistance = blockState.getBlock().getBlastResistance();

                    // Ванильное ТНТ делит сопротивление на коэф., сделаем послойное гашение:
                    // Земля (0.5) почти не задержит луч, а Камень (6.0) отнимет много сил
                    float resistanceCost = (resistance + 0.3f) * 0.3f;

                    // Вычитаем прочность блока из силы луча
                    beamPower -= resistanceCost;

                    // Если силы луча все еще хватает, чтобы разрушить этот конкретный блок
                    if (beamPower >= 0 && currentPowerCalculated(maxPower, d, radius) >= resistance) {
                        blocksToDestroy.add(currentPos.toImmutable());
                    } else {
                        // Луч полностью поглощен препятствием! Дальше за этот блок он пройти не может.
                        break;
                    }
                }

                // Естественное затухание луча от расстояния (даже в воздухе)
                beamPower -= 0.1f;
                if (beamPower <= 0) break;
            }
        }

        // Настоящее разрушение всех блоков, сквозь которые пробились лучи
        for (net.minecraft.util.math.BlockPos pos : blocksToDestroy) {
            world.breakBlock(pos, false, grenade);
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
}