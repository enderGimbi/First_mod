package me.kirill.my_first_mod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item SUPER_BREAD = new Item(new Item.Settings()
            .maxCount(8)
            .food(new FoodComponent.Builder()
                    .hunger(5)
                    .saturationModifier(0.6f)
                    .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200,1),0.1f)
                    .build()
            )
    )
// Свечение хлебушка как зачарованного
//    {
//        @Override
//        public boolean hasGlint(ItemStack stack){
//            return true;
//        }
//    }
    ;

    public static final Item BURNED_SUPER_BREAD = new Item(new Item.Settings()
            .maxCount(64)
            .food(new FoodComponent.Builder()
                    .hunger(-1)
                    .saturationModifier(0)
                    .statusEffect(new StatusEffectInstance(StatusEffects.HUNGER,200,1),1.0f)
                    .build()
            )
    );

    public static void registerModItems() {

        // Регистрация
        Registry.register(Registries.ITEM, new Identifier("my_first_mod", "super_bread"), SUPER_BREAD);
        Registry.register(Registries.ITEM,new Identifier("my_first_mod","burned_super_bread"),BURNED_SUPER_BREAD);

        // Добавление во вкладку Еды
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
            content.add(SUPER_BREAD);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
            content.add(BURNED_SUPER_BREAD);
        });
    }
}