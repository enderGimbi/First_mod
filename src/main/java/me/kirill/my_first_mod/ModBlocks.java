package me.kirill.my_first_mod;

import me.kirill.my_first_mod.block.AntiSandBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block BREAD_BOX = new Block(FabricBlockSettings.copyOf(Blocks.BIRCH_WOOD)
            .strength(2.0F,3.0F)
            .sounds(BlockSoundGroup.WOOD)
            .nonOpaque());

    public static final Block ANTI_SAND = new AntiSandBlock(FabricBlockSettings.copyOf(Blocks.SAND)
            .sounds(BlockSoundGroup.SAND)
            .strength(0.5f));

    public static void registerModBlocks(){
        // Регистрация как блока
        Registry.register(Registries.BLOCK,new Identifier("my_first_mod","bread_box"),BREAD_BOX);
        Registry.register(Registries.BLOCK,new Identifier("my_first_mod","anti_sand"),ANTI_SAND);

        // Регистрация как предмета
        Registry.register(Registries.ITEM,new Identifier("my_first_mod","bread_box"),
                new BlockItem(BREAD_BOX,new FabricItemSettings()));
        Registry.register(Registries.ITEM,new Identifier("my_first_mod","anti_sand"),
                new BlockItem(ANTI_SAND,new FabricItemSettings()));
    }

}
