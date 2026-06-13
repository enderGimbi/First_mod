package me.kirill.my_first_mod.entity;

import me.kirill.my_first_mod.ModBlocks;
import me.kirill.my_first_mod.ModEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.world.World;

public class AntiSandEntity extends Entity {

    // Объявляем переменную для хранения блока внутри класса
    private BlockState myBlockState = ModBlocks.ANTI_SAND.getDefaultState();

    // Первый конструктор для Fabric
    public AntiSandEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    // Второй конструктор для твоего спавна (5 параметров)
    public AntiSandEntity(World world, double x, double y, double z, BlockState state) {
        super(ModEntities.ANTI_SAND_TYPE, world);
        this.myBlockState = state; // Записываем переданный блок в переменную
        this.intersectionChecked = true;
        this.setPosition(x, y, z);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
    }

    @Override
    public void tick() {
        super.tick();

        // 1. Применяем скорость движения (взлетаем вверх по оси Y)
        // 0.04 — это скорость подъема. Можешь сделать больше или меньше
        this.setVelocity(this.getVelocity().add(0, 0.04, 0));

        // 2. Двигаем сущность с учетом её скорости и обрабатываем столкновения
        this.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());

        // 3. Небольшое сопротивление воздуха, чтобы скорость не росла до бесконечности
        this.setVelocity(this.getVelocity().multiply(0.98));

        // 4. ЕСЛИ ОН ВРЕЗАЛСЯ В ПОТОЛОК: Превращаем его обратно в твердый блок
        if (this.horizontalCollision || this.verticalCollision) {
            net.minecraft.util.math.BlockPos pos = this.getBlockPos();

            // Проверяем, что мир серверный и там можно поставить блок
            if (!this.getWorld().isClient && this.getWorld().getBlockState(pos).isAir()) {
                this.getWorld().setBlockState(pos, this.getBlockState()); // Ставим блок песка
                this.discard(); // Удаляем летящую сущность из мира
            }
        }
    }

    // Метод, который возвращает сохраненный блок. ОН ОБЯЗАН ТУТ БЫТЬ!
    public BlockState getBlockState() {
        return this.myBlockState;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }
}