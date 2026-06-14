package me.kirill.my_first_mod.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.World;

public class GrenadeEntity extends ThrownItemEntity {

    public float explosionPower = 4.0f;
    public int fuseTicks = 20;
    private boolean hasCollided = false;
    private boolean isResetedVelocity = false;

    public GrenadeEntity(EntityType<GrenadeEntity> entityType, World world) {
        super(entityType, world);
    }

    public GrenadeEntity(EntityType<GrenadeEntity> entityType, LivingEntity livingEntity, World world) {
        super(entityType, livingEntity, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.TNT;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        if(!hasCollided){
            isResetedVelocity = true;
            this.setVelocity(0.0,0.0,0.0);
        }
        hasCollided = true;
        super.onCollision(hitResult);
    }

    @Override
    public void tick() {
        // 1. Если граната уже ударилась о пол, мы ЖЕСТКО держим её скорость на нуле,
        // чтобы ванильная гравитация из super.tick() не утащила её под землю!
        if (hasCollided) {
            this.setVelocity(0.0, 0.0, 0.0);
        } else {
            // Проверяем столкновение только если мы ещё летим
            HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
            if (hitResult.getType() != HitResult.Type.MISS) {
                if (!isResetedVelocity) {
                    this.onCollision(hitResult);
                }
            }
        }

        // 2. Вызываем супер-метод (он обсчитает движение, но скорость теперь гарантированно занулена)
        super.tick();

        // 3. Логика таймера взрыва
        if (hasCollided) {
            --fuseTicks;
        }

        if (fuseTicks <= 0) {
            // Взрываем строго на сервере
            if (!this.getWorld().isClient) {
                explosion(super.getWorld(), this);
            }
            super.discard();
        }
    }

    public void explosion(World world, Entity entity){
        world.createExplosion(this,this.getX(),this.getY(),this.getZ(),explosionPower,World.ExplosionSourceType.TNT);
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose){
        return EntityDimensions.changing(0.98f,0.98f);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
