package me.kirill.my_first_mod.entity;

import me.kirill.my_first_mod.ModEntities;
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

    public float explosionPower = 4.0f;
    public int fuseTicks = 0;
    private static final TrackedData<Boolean> HAS_COLLIDED = DataTracker.registerData(GrenadeEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private UUID ownerUuid;

    public GrenadeEntity(EntityType<GrenadeEntity> entityType, World world) {
        super(entityType, world);
    }

    public GrenadeEntity(World world, LivingEntity owner) {
        super(ModEntities.GRENADE_TYPE, owner, world);
        if (owner != null) {
            this.ownerUuid = owner.getUuid();
        }
    }

    public GrenadeEntity(EntityType<? extends ThrownItemEntity> entityType, LivingEntity livingEntity, World world) {
        super(entityType, livingEntity, world);
        if (livingEntity != null) {
            this.ownerUuid = livingEntity.getUuid();
        }
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
            --fuseTicks;
            if (fuseTicks <= 0) {
                explosion(this.getWorld(), this);
                this.discard();
            }
        }
    }

    public void explosion(World world, Entity entity) {
        world.createExplosion(this, this.getX(), this.getY(), this.getZ(), explosionPower, World.ExplosionSourceType.TNT);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    protected boolean canHit(Entity entity) {
        if (this.ownerUuid != null && entity.getUuid().equals(this.ownerUuid)) {
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
    }

    public boolean isHasCollided() {
        return this.dataTracker.get(HAS_COLLIDED);
    }

    public void setHasCollided(boolean collided) {
        this.dataTracker.set(HAS_COLLIDED, collided);
    }
}