package me.kirill.my_first_mod.block;

import me.kirill.my_first_mod.ModBlocks;
import me.kirill.my_first_mod.entity.AntiSandEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class AntiSandBlock extends Block {
    public AntiSandBlock(Settings settings) {
        super(settings);
    }

    // Срабатывает, когда блок только поставили в мир
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, this, 2); // Заводим таймер проверки на через 2 тика
    }

    // Срабатывает, если обновились соседи (например, сломали блок сверху)
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        world.scheduleBlockTick(pos, this, 2);
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    // Сам тик, где происходит превращение блока в сущность
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        // Проверяем: если сверху воздух/жидкость и мы не на самом краю неба
        if (canFallUp(world.getBlockState(pos.up())) && pos.getY() < world.getTopY()) {
            // Создаем нашу кастомную сущность (смещение на +0.5 нужно для центровки по блоку)
            BlockState state1 = ModBlocks.ANTI_SAND.getDefaultState();
            AntiSandEntity entity = new AntiSandEntity(world, (double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5, state1);

            // Превращаем блок в мире в воздух
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);

            // Спавним сущность лететь вверх
            world.spawnEntity(entity);
        }
    }

    // Вспомогательный метод: определяет, сквозь что наш песок может лететь вверх
    private static boolean canFallUp(BlockState state) {
        return state.isAir() || state.isOf(Blocks.FIRE) || !state.getFluidState().isEmpty();
    }
}