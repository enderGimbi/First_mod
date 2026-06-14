package me.kirill.my_first_mod;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup MY_MOD_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier("my_first_mod","my_mod_group"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.my_mod_group"))
                    .icon(()->new ItemStack(ModBlocks.BREAD_BOX))
                    .entries((displayContext, entries) -> {
                        // Предметы во вкладке
                        entries.add(ModBlocks.BREAD_BOX);
                        entries.add(ModBlocks.ANTI_SAND);
                        entries.add(ModItems.SUPER_BREAD);
                        entries.add(ModItems.BURNED_SUPER_BREAD);
                        entries.add(ModItems.GLAUNCHER);
                        entries.add(ModItems.GLAUNCHER_V2);
                    })
                    .build());

    // Пустышка, чтобы класс прогрузился
    public static void registerItemGroups()
    {

    }
}
