package me.kirill.my_first_mod;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModPotions {

    // Тестовое зелье голода (чтобы удобно было хлебушки и еду в целом тестить)
    public static final Potion HUNGER_POTION = new Potion(
            "hunger_potion",
            new StatusEffectInstance(StatusEffects.HUNGER,200,255)
    );

    public static void registerModPotions(){
        // Регистрация
        Registry.register(Registries.POTION,new Identifier("my_first_mod","hunger_potion"),HUNGER_POTION);

        // Также в идеале иметь функцию для регистрации рецептов зелий (рецепты для зелий пишутся в Java файлах)
    }

    public static void registerPotionRecipes(){
        BrewingRecipeRegistry.registerPotionRecipe(
                Potions.AWKWARD,
                ModItems.BURNED_SUPER_BREAD,
                HUNGER_POTION
        );
    }
}
