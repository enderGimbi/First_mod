package me.kirill.my_first_mod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {

    public static final Item SUPER_BREAD = new Item(new Item.Settings()
            .fireproof()
            .maxCount(8)
            .food(new FoodComponent.Builder()
                    .hunger(5)
                    .saturationModifier(0.6f)
                    .build()
            )
    ){
        @Override
        public boolean hasGlint(ItemStack stack){
            return true;
        }
    };


    public static void registerModItems() {

        // Регистрация
        Registry.register(Registries.ITEM, new Identifier("my_first_mod", "super_bread"), SUPER_BREAD);

        // Добавление во вкладку Еды
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
            content.add(SUPER_BREAD);
        });
    }
}