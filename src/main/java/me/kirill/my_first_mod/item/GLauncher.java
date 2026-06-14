package me.kirill.my_first_mod.item;

import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GLauncher extends Item {

    public GLauncher(Settings settings) {
        super(settings);
    }

    // Логика для ПКМ, в который умещаем выстрел и перезарядку
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
        ItemStack stack = user.getStackInHand(hand);
        var nbt = user.getStackInHand(hand).getOrCreateNbt(); // для проверки зарядки
        boolean checkLoaded = nbt.getBoolean("loaded");
        if(checkLoaded){

            // если заряжен, то стреляем
            stack.damage(1,user,player -> {});
            user.getItemCooldownManager().set(this,10);

            // на стороне сервера
            if(!world.isClient()){
            nbt.putBoolean("loaded",false);
            shot(world, user);
            }

            return TypedActionResult.success(stack,false);
        }
        else{
            if(canReaload(user)){
                user.getItemCooldownManager().set(this,10);
                if(!world.isClient()){
                    reload(user, nbt);
                }
                return TypedActionResult.success(stack,false);
            }
            else
                return TypedActionResult.fail(stack);
        }
    }

    public void shot(World world,PlayerEntity player){
        Vec3d vector = player.getRotationVector();
        TntEntity tnt = new TntEntity(world,player.getX(),player.getY()+player.getEyeHeight(player.getPose()),player.getZ(),player);
        tnt.setVelocity(vector.multiply(2.0f));
        tnt.setFuse(40);
        world.playSound(null,
                player.getBlockPos(),
                SoundEvents.ENTITY_WITHER_SHOOT,
                SoundCategory.PLAYERS,
                1.0f,
                0.6f);
        world.spawnEntity(tnt);
    }

    public void reload(PlayerEntity user, NbtCompound nbt){
        if(!user.isCreative()){
            user.getInventory().remove(stack -> stack.isOf(Items.TNT),1,user.getInventory());
        }
        nbt.putBoolean("loaded",true);
    }

    public boolean canReaload(PlayerEntity player){
        return player.getInventory().contains(Items.TNT.asItem().getDefaultStack()) || player.isCreative();
    }
}
