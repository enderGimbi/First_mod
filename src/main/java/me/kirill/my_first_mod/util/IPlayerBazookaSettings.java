package me.kirill.my_first_mod.util;

public interface IPlayerBazookaSettings {
    float getExplosionPower();
    void setExplosionPower(float power);

    int getFuseDelay();
    void setFuseDelay(int delay);

    float getShootVelocity();
    void setShootVelocity(float velocity);

    float getSoundVolume();
    void setSoundVolume(float volume);
}