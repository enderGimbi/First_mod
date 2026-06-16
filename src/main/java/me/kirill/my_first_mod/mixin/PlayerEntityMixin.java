package me.kirill.my_first_mod.mixin;

import me.kirill.my_first_mod.My_first_mod;
import me.kirill.my_first_mod.util.IPlayerBazookaSettings;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements IPlayerBazookaSettings {

    // Привязываем базовые значения к твоему классу конфигурации!
    @Unique private float bazookaExplosionPower = My_first_mod.config.explosionPower;
    @Unique private int bazookaFuseDelay = My_first_mod.config.fuseDelayTicks;
    @Unique private float bazookaShootVelocity = My_first_mod.config.shootVelocity;
    @Unique private float bazookaSoundVolume = My_first_mod.config.soundVolume;

    @Override
    public float getExplosionPower() { return bazookaExplosionPower; }
    @Override
    public void setExplosionPower(float power) { this.bazookaExplosionPower = power; }

    @Override
    public int getFuseDelay() { return bazookaFuseDelay; }
    @Override
    public void setFuseDelay(int delay) { this.bazookaFuseDelay = delay; }

    @Override
    public float getShootVelocity() { return bazookaShootVelocity; }
    @Override
    public void setShootVelocity(float velocity) { this.bazookaShootVelocity = velocity; }

    @Override
    public float getSoundVolume() { return bazookaSoundVolume; }
    @Override
    public void setSoundVolume(float volume) { this.bazookaSoundVolume = volume; }
}