package me.kirill.my_first_mod.item;

import me.kirill.my_first_mod.ModEntities;
import me.kirill.my_first_mod.entity.GrenadeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class Glauncher_v2 extends GLauncher{

    public Glauncher_v2(Settings settings) {
        super(settings);
    }

    @Override
    public void shot(World world, PlayerEntity player){
        GrenadeEntity grenade = new GrenadeEntity(ModEntities.GRANADE_TYPE,player,world);
        grenade.setVelocity(player,player.getPitch(),player.getYaw(),0.0f,1.0f,0.1f);
        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.ENTITY_WITHER_SHOOT,
                SoundCategory.PLAYERS,
                1.0f,
                0.4f);
        world.spawnEntity(grenade);
    }

}
